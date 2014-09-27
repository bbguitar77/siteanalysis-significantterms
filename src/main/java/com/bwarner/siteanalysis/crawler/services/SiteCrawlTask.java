package com.bwarner.siteanalysis.crawler.services;

import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwarner.siteanalysis.crawler.exception.HttpException;
import com.bwarner.siteanalysis.crawler.model.HttpResponse;
import com.bwarner.siteanalysis.crawler.model.SiteCrawlInfo;

public class SiteCrawlTask implements Callable<SiteCrawlInfo> {

  private static Logger log = LoggerFactory.getLogger(SiteCrawlTask.class);

  static HttpService    httpService;

  final public String   uri;
  final public int      depth;

  public SiteCrawlTask(final String uri, final int depth) {
    this.uri = uri;
    this.depth = depth;
  }

  @Override
  public SiteCrawlInfo call() throws Exception {
    HttpResponse httpResonse = null;
    Set<String> extractedLinks = null;
    try {
      httpResonse = httpService.doGet(uri);
      if (HttpStatus.SC_OK == httpResonse.status)
        extractedLinks = WebTextAnalyzer.extractLinks(uri, httpResonse.content);
    }
    catch (HttpException he) {
      log.error("Could not process HTTP request for URI '{}', skipping...", uri);
    }
    catch (IllegalArgumentException iae) {
      log.error("Crawling task was provided a blank URI, skipping...");
    }
    catch (Exception e) {
      log.error("Unknown error occurred within crawling task...", e);
    }

    return new SiteCrawlInfo(uri, depth, httpResonse, extractedLinks);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SiteCrawlTask sct = (SiteCrawlTask) obj;
    return StringUtils.equals(this.uri, sct.uri);
  }

  @Override
  public int hashCode() {
    return uri.hashCode();
  }
}
