package com.bwarner.siteanalysis.crawler.model;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

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

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SiteCrawlInfo sci = (SiteCrawlInfo) obj;
    if (null != httpResponse && null != sci.httpResponse)
      return StringUtils.equals(httpResponse.uri, sci.httpResponse.uri);
    if (null == httpResponse && null == sci.httpResponse)
      return StringUtils.equals(requestUri, sci.requestUri);
    return false;
  }

  @Override
  public int hashCode() {
    String uri = (null != httpResponse) ? httpResponse.uri : requestUri;
    return uri.hashCode();
  }
}
