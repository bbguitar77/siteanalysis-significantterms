package com.bwarner.siteanalysis.integration.model;

import java.net.URI;

import org.apache.commons.lang.StringUtils;

import com.bwarner.siteanalysis.crawler.model.CrawlOptions.RestrictionPolicy;
import com.bwarner.siteanalysis.crawler.services.URINormalizer;

/**
 * Options for Site Analysis task(s)
 */
public class SiteAnalysisOptions {

  public final URI               uri;
  public int                     maxDepth;
  public final RestrictionPolicy restrictionPolicy;

  private SiteAnalysisOptions(SiteAnalysisOptionsBuilder builder) {
    this.uri = builder.uri;
    this.maxDepth = builder.maxDepth;
    this.restrictionPolicy = builder.restrictionPolicy;
  }

  public static class SiteAnalysisOptionsBuilder {
    private URI               uri;
    private int               maxDepth          = 2;
    private RestrictionPolicy restrictionPolicy = RestrictionPolicy.HOST;

    public SiteAnalysisOptionsBuilder() {
    }

    public SiteAnalysisOptions build() {
      return new SiteAnalysisOptions(this);
    }

    /* GETTERS & SETTERS */
    public SiteAnalysisOptionsBuilder setUri(String uri) {
      this.uri = URI.create(URINormalizer.normalize(uri));
      return this;
    }

    public SiteAnalysisOptionsBuilder setMaxDepth(int maxDepth) {
      this.maxDepth = maxDepth;
      return this;
    }

    public void setRestrictionPolicy(String restrictionPolicy) {
      this.restrictionPolicy = RestrictionPolicy.valueOf(StringUtils.upperCase(restrictionPolicy));
    }
  }
}
