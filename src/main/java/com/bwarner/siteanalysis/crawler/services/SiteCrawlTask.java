package com.bwarner.siteanalysis.crawler.services;

import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwarner.siteanalysis.crawler.exception.HttpException;
import com.bwarner.siteanalysis.crawler.model.CrawlOptions;
import com.bwarner.siteanalysis.crawler.model.HttpResponse;
import com.bwarner.siteanalysis.crawler.model.SiteCrawlInfo;

/**
 * {@link Callable} task for crawling a URI at a specified depth with the given
 * {@link CrawlOptions} settings
 */
public class SiteCrawlTask implements Callable<SiteCrawlInfo> {

  private static Logger     log = LoggerFactory.getLogger(SiteCrawlTask.class);

  static HttpService        httpService;

  final public URI          requestURI;
  final public int          requestDepth;
  final public CrawlOptions options;

  public SiteCrawlTask(final URI requestURI, final int depth, final CrawlOptions options) {
    this.requestURI = requestURI;
    this.requestDepth = depth;
    this.options = options;
  }

  @Override
  public SiteCrawlInfo call() throws Exception {
    HttpResponse response = null;
    Set<URI> links = null;
    try {
      response = httpService.doGet(requestURI.toString());
      if (HttpStatus.SC_OK == response.status)
        links = WebTextAnalyzer.extractLinks(requestURI.toString(), response.content, options);
    }
    catch (HttpException he) {
      log.error("Could not process HTTP request for URI '{}', skipping...", requestURI.toString());
    }
    catch (Exception e) {
      log.error("Unknown error occurred within crawling task...", e);
    }

    return new SiteCrawlInfo(requestURI, requestDepth, response, links);
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
    return Objects.equals(requestURI, sct.requestURI);
  }

  @Override
  public int hashCode() {
    return requestURI.hashCode();
  }
}
