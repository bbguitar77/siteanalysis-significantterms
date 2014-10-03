package com.bwarner.siteanalysis.crawler.services;

import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URINormalizer {

  private static Logger       log            = LoggerFactory.getLogger(URINormalizer.class);

  private static final int    DEFAULT_PORT   = 80;
  private static final String DEFAULT_SCHEME = "http";

  public static String normalize(final String uri) throws IllegalArgumentException {
    if (StringUtils.isBlank(uri))
      throw new IllegalArgumentException("URI can't be blank");

    if (isLinkRelative(uri))
      throw new IllegalArgumentException("Base URI is required for relative path");

    try {
      String normUri = uri.toLowerCase();
      normUri = prefixScheme(normUri);

      URIBuilder ret = new URIBuilder(normUri);
      final String host = ret.getHost();
      // strip www. from host
      if (host != null && host.contains("www."))
        ret.setHost(host.replaceFirst("www.", ""));
      // strip default port
      if (DEFAULT_PORT == ret.getPort())
        ret.setPort(-1);
      // clear query parameters
      ret.clearParameters();
      // clear fragment
      ret.setFragment(null);

      normUri = ret.build().toString();
      // omit trailing /, #
      if (normUri.matches(".*[/#]$"))
        normUri = normUri.substring(0, normUri.length() - 1);
      return normUri;
    }
    catch (URISyntaxException e) {
      log.error("Could not normalize URI: {}", uri, e);
      throw new IllegalArgumentException("Malformed URI");
    }
  }

  public static String normalize(final String baseURI, final String target) throws IllegalArgumentException,
                                                                           URISyntaxException {
    StringBuilder ret = new StringBuilder();
    if (isLinkRelative(target)) {
      ret.append(baseURI);
      ret.append(target);
    }
    else {
      ret.append(target);
    }
    return normalize(ret.toString());
  }

  protected static String prefixScheme(final String uri) {
    if (!uri.matches("^(http|https|feed|ftp|emailto):.*"))
      return DEFAULT_SCHEME + "://" + uri;
    else
      return uri;
  }

  protected static boolean isLinkRelative(final String uri) {
    return StringUtils.defaultString(uri).matches("^[/#].*");
  }
}
