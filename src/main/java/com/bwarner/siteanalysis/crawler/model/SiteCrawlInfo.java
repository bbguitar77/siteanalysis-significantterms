package com.bwarner.siteanalysis.crawler.model;

import java.util.Set;

/**
 * Represents information retrieved from crawling a particular site
 *
 * @author bwarner
 */
public class SiteCrawlInfo {

  final public String       requestUri;
  final public int          requestDepth;
  final public HttpResponse httpResponse;
  final public Set<String>  extractedLinks;

  public SiteCrawlInfo(final String requestUri,
                       final int requestDepth,
                       final HttpResponse httpResponse,
                       final Set<String> extractedLinks) {
    this.requestUri = requestUri;
    this.requestDepth = requestDepth;
    this.httpResponse = httpResponse;
    this.extractedLinks = extractedLinks;
  }

  public boolean isSuccess() {
    return (null != httpResponse && 200 == httpResponse.status);
  }

  @Override
  public String toString() {
    return String.format("Request URI: %s, Request Depth: [%d], Http Response: {%s}, # Extracted Links: [%d]",
                         requestUri,
                         requestDepth,
                         (null != httpResponse ? httpResponse.toString() : "null"),
                         (null != extractedLinks ? extractedLinks.size() : 0));
  }
}
