package com.bwarner.siteanalysis.integration.services;

import java.util.Set;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import com.bwarner.siteanalysis.crawler.exception.CrawlingException;
import com.bwarner.siteanalysis.crawler.model.CrawlOptions;
import com.bwarner.siteanalysis.crawler.model.SiteCrawlInfo;
import com.bwarner.siteanalysis.crawler.services.SiteCrawlingService;
import com.bwarner.siteanalysis.integration.model.SiteAnalysisOptions;
import com.bwarner.siteanalysis.search.exception.SearchIndexingException;
import com.bwarner.siteanalysis.search.model.SiteDocument;
import com.bwarner.siteanalysis.search.services.SearchIndexingService;

/**
 * Integration service connecting the {@link SiteCrawlingService} and the
 * {@link SearchIndexingService}
 *
 * @author bwarner
 */
@Service
public class SiteAnalysisService {

  @SuppressWarnings("unused")
  private static Logger             log = LoggerFactory.getLogger(SiteAnalysisService.class);

  @Autowired
  private SiteCrawlingService       siteCrawlingService;

  @Autowired
  private SearchIndexingService     searchIndexingService;

  @Autowired
  private Transformer<SiteDocument> transformer;

  @Async
  public Future<Boolean> analyzeSite(SiteAnalysisOptions options) throws CrawlingException, SearchIndexingException {
    // crawl
    Set<SiteCrawlInfo> crawlResults = siteCrawlingService.crawl(new CrawlOptions(options.uri, options.maxDepth));
    // transform
    SiteDocument[] siteDocs = transformer.transform(crawlResults.toArray(new SiteCrawlInfo[0]));
    // index
    searchIndexingService.indexSites(siteDocs);
    // future proxy
    return new AsyncResult<Boolean>(true);
  }

  /* GETTERS & SETTERS */
  public void setSiteCrawlingService(SiteCrawlingService siteCrawlingService) {
    this.siteCrawlingService = siteCrawlingService;
  }

  public void setSearchIndexingService(SearchIndexingService searchIndexingService) {
    this.searchIndexingService = searchIndexingService;
  }

  public void setTransformer(Transformer<SiteDocument> transformer) {
    this.transformer = transformer;
  }
}
