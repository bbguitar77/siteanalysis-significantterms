package com.bwarner.siteanalysis.search.model;

/**
 * Represents a Site document in ElasticSearch
 *
 * @author bwarner
 */
public class SiteDocument {

  public final String uri;

  public final String content;

  public SiteDocument(final String uri, final String content) {
    this.uri = uri;
    this.content = content;
  }
}
