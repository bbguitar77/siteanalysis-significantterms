package com.bwarner.siteanalysis.crawler.services;

import java.util.Set;

import com.bwarner.siteanalysis.crawler.exception.CrawlingException;
import com.bwarner.siteanalysis.crawler.model.SiteCrawlInfo;

public interface SiteCrawlingService {

  /**
   * Recursively crawls a seed URI (via HTTP) up to the specified max-depth.
   * This is intended to be a blocking action and will not return until the
   * crawl is complete
   *
   * TODO: Think about returning a Future so the caller can decide when to block
   * on Future#get()
   *
   * @param uri
   *          Seed Request URI
   * @param maxDepth
   *          Max recursive depth
   * @return {@link SiteCrawlInfo} results
   * @throws CrawlingException
   */
  public Set<SiteCrawlInfo> crawl(final String uri, final int maxDepth) throws CrawlingException;
}
