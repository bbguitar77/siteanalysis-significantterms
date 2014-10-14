package com.bwarner.siteanalysis.crawler.model;

import java.net.URI;

import com.bwarner.siteanalysis.crawler.services.URINormalizer;

public class CrawlOptions {

  final public URI               seedURI;
  final public int               maxDepth;
  final public RestrictionPolicy restrictionPolicy;

  // HOST => domain + sub-domain equality
  public enum RestrictionPolicy {
    DOMAIN, HOST
  }

  public CrawlOptions(final String uri, final int maxDepth) throws IllegalArgumentException {
    this(URI.create(URINormalizer.normalize(uri)), maxDepth);
  }

  public CrawlOptions(final URI uri, final int maxDepth) {
    this(uri, maxDepth, RestrictionPolicy.HOST);
  }

  public CrawlOptions(final URI uri, final int maxDepth, RestrictionPolicy rPolicy) {
    this.seedURI = uri;
    this.maxDepth = maxDepth;
    this.restrictionPolicy = rPolicy;
  }
}
