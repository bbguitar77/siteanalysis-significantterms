package com.bwarner.siteanalysis.search.model;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.UUID;

import org.apache.nutch.util.URLUtil;

/**
 * Embodies an ElasticSearch Site document
 */
public class SiteDocument {

  public final String content;
  public final String domain;
  public final String host;
  public final String id;
  public final URI    uri;

  public SiteDocument(final String uri, final String content) throws IllegalArgumentException {
    this(URI.create(uri), content);
  }

  public SiteDocument(final URI uri, final String content) throws IllegalArgumentException {
    if (uri == null)
      throw new IllegalArgumentException("URI cannot be null");

    try {
      this.content = content;
      this.domain = URLUtil.getDomainName(uri.toURL());
      this.host = uri.getHost();
      this.id = generateUUIDNoDashes(uri.toString());
      this.uri = uri;
    }
    catch (MalformedURLException e) { // shouldn't happen
      throw new IllegalArgumentException("Malformed URI");
    }
  }

  private static String generateUUIDNoDashes(String input) {
    return UUID.nameUUIDFromBytes(input.getBytes()).toString().replaceAll("[-]", "");
  }
}
