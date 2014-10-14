package com.bwarner.siteanalysis.crawler.services;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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
      URIBuilder uriBuilder = new URIBuilder(uri.toLowerCase());
      // prefix default scheme, if absent
      if (StringUtils.isBlank(uriBuilder.getScheme()) || "https".equals(uriBuilder.getScheme()))
        uriBuilder.setScheme(DEFAULT_SCHEME);

      if (DEFAULT_SCHEME.equals(uriBuilder.getScheme())) {
        // strip www. from host
        final String host = uriBuilder.getHost();
        if (host != null && host.contains("www."))
          uriBuilder.setHost(host.replaceFirst("www.", ""));
        // strip default port
        if (DEFAULT_PORT == uriBuilder.getPort())
          uriBuilder.setPort(-1);
        // strip query parameters
        uriBuilder.clearParameters();
        // strip fragment (i.e. anchor)
        uriBuilder.setFragment(null);
      }

      String ret = uriBuilder.build().toString();
      // omit trailing slash '/'
      if (ret.endsWith("/"))
        ret = ret.substring(0, ret.length() - 1);
      return ret;
    }
    catch (URISyntaxException e) {
      log.error("Could not normalize URI: {}", uri, e);
      throw new IllegalArgumentException("Malformed URI");
    }
  }

  public static String normalize(final String baseURI, final String target) throws IllegalArgumentException {
    String ret = null;
    if (isLinkRelative(target)) {
      try {
        URL context = new URL(baseURI);
        ret = new URL(context, target).toString();
      }
      catch (MalformedURLException e) {
        throw new IllegalArgumentException(e);
      }
    }
    else {
      ret = target;
    }
    return normalize(ret);
  }

  protected static boolean isLinkRelative(final String uri) {
    return StringUtils.isBlank(URI.create(uri).getScheme());
  }
}
