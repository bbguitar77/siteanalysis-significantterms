package com.bwarner.siteanalysis.search.services;

import com.bwarner.siteanalysis.search.exception.SearchIndexingException;
import com.bwarner.siteanalysis.search.model.SiteDocument;

public interface SearchIndexingService {

  public void indexSites(SiteDocument[] sites) throws SearchIndexingException;
}
