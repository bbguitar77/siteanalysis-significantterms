package com.bwarner.siteanalysis.integration.services;

import com.bwarner.siteanalysis.crawler.model.SiteCrawlInfo;

public interface Transformer<T> {

  public T[] transform(SiteCrawlInfo[] items);
}
