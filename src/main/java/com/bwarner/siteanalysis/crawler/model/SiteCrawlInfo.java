package com.bwarner.siteanalysis.crawler.model;

import java.net.URI;
import java.util.Objects;
import java.util.Set;

import org.apache.http.HttpStatus;

/**
 * Represents information retrieved from crawling a particular site
 */
public class SiteCrawlInfo {

  final public URI          requestURI;
  final public int          requestDepth;
  final public HttpResponse responseData;
  final public Set<URI>     links;

  public SiteCrawlInfo(final URI requestURI,
                       final int requestDepth,
                       final HttpResponse responseData,
                       final Set<URI> links) {
    this.requestURI = requestURI;
    this.requestDepth = requestDepth;
    this.responseData = responseData;
    this.links = links;
  }

  public boolean isSuccess() {
    return (null != responseData && HttpStatus.SC_OK == responseData.status);
  }

  @Override
  public String toString() {
    return String.format("Request URI: %s, Request Depth: [%d], Http Response: {%s}, # Extracted Links: [%d]",
                         requestURI.toString(),
                         requestDepth,
                         (null != responseData ? responseData.toString() : "null"),
                         (null != links ? links.size() : 0));
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
    if (null != responseData && null != sci.responseData)
      return Objects.equals(responseData.uri, sci.responseData.uri);
    if (null == responseData && null == sci.responseData)
      return Objects.equals(this.requestURI, sci.requestURI);
    return false;
  }

  @Override
  public int hashCode() {
    URI uri = (null != responseData) ? responseData.uri : requestURI;
    return uri.hashCode();
  }
}
