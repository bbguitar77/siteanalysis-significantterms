package com.bwarner.siteanalysis.integration.model;

import java.net.URI;

import com.bwarner.siteanalysis.crawler.services.URINormalizer;

/**
 * Options for Site Analysis task(s)
 */
public class SiteAnalysisOptions {

  public final URI uri;
  public int       maxDepth;

  private SiteAnalysisOptions(SiteAnalysisOptionsBuilder builder) {
    this.uri = builder.getUri();
    this.maxDepth = builder.getMaxDepth();
  }

  public static class SiteAnalysisOptionsBuilder {
    private URI uri;
    private int maxDepth = 2; // default depth

    public SiteAnalysisOptionsBuilder() {
    }

    public SiteAnalysisOptions build() {
      return new SiteAnalysisOptions(this);
    }

    /* GETTERS & SETTERS */
    public URI getUri() {
      return uri;
    }

    public SiteAnalysisOptionsBuilder setUri(String uri) {
      this.uri = URI.create(URINormalizer.normalize(uri));
      return this;
    }

    public int getMaxDepth() {
      return maxDepth;
    }

    public SiteAnalysisOptionsBuilder setMaxDepth(int maxDepth) {
      this.maxDepth = maxDepth;
      return this;
    }
  }
}
