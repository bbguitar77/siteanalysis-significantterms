package com.bwarner.siteanalysis.crawler.services;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WebTextAnalyzer {

  private static Logger        log         = LoggerFactory.getLogger(WebTextAnalyzer.class);

  private static final Pattern hrefPattern = Pattern.compile(String.format("<a\\s+href=\"([^\\s]+)\""),
                                                             Pattern.CASE_INSENSITIVE);

  private static final Pattern uriPattern  = Pattern.compile("^((http[s]?|ftp|feed):/)?/?([^:/\\s?#]+)(:([\\d]*))?((/[^:/\\s?]+)*/)?([!~%_@]?[\\w]+[\\w\\-\\.]*[^#?\\s]*)?(\\?([^#]*))?(#(.*))?$");

  public static enum UriSection {
    PROTOCOL, HOST, PORT, PATH, FILE, QUERYSTRING, HASH
  };

  /**
   * Method to extract a set of links from the HTML page content, constrained to
   * the domain of the request URI
   *
   * @param uri
   * @param pageContent
   * @return
   */
  public static Set<String> extractLinks(final String uri, final String pageContent) {

    if (StringUtils.isBlank(uri))
      throw new IllegalArgumentException("URI value must be provided in order to validate extracted links");

    final String originDomain = getDomainForUri(uri);

    Set<String> ret = new HashSet<>();
    Matcher m = hrefPattern.matcher(pageContent);
    while (m.find()) {
      String link = m.group(1);
      if (validateLink(originDomain, link)) {
        ret.add(sanitizeLink(uri, link));
      }
    }

    if (log.isTraceEnabled()) {
      log.trace("Extacted {} links from Uri '{}'", ret.size(), uri);
      if (ret.size() > 0)
        log.trace("==> {}", Arrays.toString(ret.toArray(new String[0])));
    }

    return ret;
  }

  final static private String[] uriBreaks = { "?", "#" };

  protected static String sanitizeLink(String uri, String link) {
    String ret = link;
    for (String brk : uriBreaks) {
      if (ret.contains(brk))
        ret = ret.split(Pattern.quote(brk))[0];
    }

    // relative links need to be appended to the originating request Uri
    if (isLinkRelative(ret)) {
      ret = uri.concat(ret);
    }

    return ret.toLowerCase();
  }

  /**
   * Validates to see whether an extracted link belongs to the same domain as
   * the originating request URI
   *
   * @param domain
   *          Domain of the originating request URI
   * @param link
   *          URI to validate
   * @return
   */
  protected static boolean validateLink(final String domain, final String link) {
    // consider only http[s] links and relative paths "/" as valid
    final String protocol = extractFromUri(link, UriSection.PROTOCOL);
    final boolean isLinkRelative = isLinkRelative(link);
    if ((StringUtils.isBlank(protocol) && !isLinkRelative) || (StringUtils.isNotBlank(protocol) && !protocol.matches("http[s]?"))) {
      log.trace("Skipping Link '{}' - failed HTTP protocol validation", link);
      return false;
    }

    final String linkDomain = getDomainForUri(link);
    if (!StringUtils.equalsIgnoreCase(domain, linkDomain) && !isLinkRelative) {
      log.trace("Skipping Link '{}' - failed domain validation for origin '{}'", link, domain);
      return false;
    }

    return true;
  }

  protected static boolean isLinkRelative(final String uri) {
    return StringUtils.defaultString(uri).matches("^/.+");
  }

  /**
   * Extracts the domain for a given URL
   *
   * @param url
   * @return
   */
  protected static String getDomainForUri(String uri) {
    String ret = extractFromUri(uri, UriSection.HOST);
    if (null != ret) {
      ret = ret.toLowerCase();
      // Strip out leading www.
      if (ret.startsWith("www."))
        ret = ret.replace("www.", "");
    }

    return ret;
  }

  protected static String extractFromUri(String uri, UriSection section) {
    if (StringUtils.isBlank(uri))
      return null;

    try {
      String ret = null;
      Matcher m = uriPattern.matcher(uri.trim());
      m.find();
      switch (section) {
        case PROTOCOL:
          ret = m.group(2);
          break;
        case HOST:
          ret = m.group(3);
          break;
        case PORT:
          ret = m.group(5);
          break;
        case PATH:
          ret = m.group(6);
          break;
        case FILE:
          ret = m.group(8);
          break;
        case QUERYSTRING:
          ret = m.group(10);
          break;
        case HASH:
          ret = m.group(12);
          break;
        default:
          break;
      }
      return ret;
    }
    catch (IllegalStateException Ignore) {
      return null;
    }
    catch (Exception Ignore) {
      return null;
    }
  }
}
