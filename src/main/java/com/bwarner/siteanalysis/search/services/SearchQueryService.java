package com.bwarner.siteanalysis.search.services;

import com.bwarner.siteanalysis.search.exception.SearchQueryException;
import com.bwarner.siteanalysis.search.model.SignificantTermsQueryResponse;

public interface SearchQueryService {

  public SignificantTermsQueryResponse getSignificantTerms(final String query) throws SearchQueryException;
}
