package com.bwarner.siteanalysis.search.model;

import java.util.List;

/**
 * Response container for ElasticSearch Significant Term aggregations
 */
public class SignificantTermsQueryResponse {

  public final List<SignificantTerm> significantTerms;

  public SignificantTermsQueryResponse(List<SignificantTerm> significantTerms) {
    this.significantTerms = significantTerms;
  }

  public static class SignificantTerm {

    public final String key;
    public final double significanceScore;
    public final long   hits;
    public final long   subsetSize;
    public final long   supersetSize;

    public SignificantTerm(final String key,
                           final Double significanceScore,
                           long hits,
                           long subsetSize,
                           long supersetSize) {
      this.key = key;
      this.significanceScore = significanceScore;
      this.hits = hits;
      this.subsetSize = subsetSize;
      this.supersetSize = supersetSize;
    }

    @Override
    public String toString() {
      return String.format("Term: %s, Significance Score: %s, Frequency (hits/foreground/background): %s/%s/%s",
                           key,
                           significanceScore,
                           hits,
                           subsetSize,
                           supersetSize);
    }
  }
}
