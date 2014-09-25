package com.bwarner.siteanalysis.integration.model;

/**
 * Options for Site Analysis task(s)
 *
 * @author bwarner
 */
public class SiteAnalysisOptions {

  public final String uri;
  public int          maxDepth;

  private SiteAnalysisOptions(SiteAnalysisOptionsBuilder builder) {
    this.uri = builder.getUri();
    this.maxDepth = builder.getMaxDepth();
  }

  public static class SiteAnalysisOptionsBuilder {
    private String uri;
    private int    maxDepth = 0;

    public SiteAnalysisOptionsBuilder() {
    }

    public SiteAnalysisOptions build() {
      return new SiteAnalysisOptions(this);
    }

    /* GETTERS & SETTERS */
    public String getUri() {
      return uri;
    }

    public SiteAnalysisOptionsBuilder setUri(String uri) {
      this.uri = uri;
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
