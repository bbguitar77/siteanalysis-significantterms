package com.bwarner.siteanalysis.crawler.services;

import java.util.Set;

import com.bwarner.siteanalysis.crawler.exception.CrawlingException;
import com.bwarner.siteanalysis.crawler.model.SiteCrawlInfo;

public interface SiteCrawlingService {

  public Set<SiteCrawlInfo> crawl(final String uri, final int maxDepth) throws CrawlingException;
}
