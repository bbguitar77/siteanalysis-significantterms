package com.bwarner.siteanalysis.crawler.services;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.nutch.util.URLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bwarner.siteanalysis.crawler.model.CrawlOptions;

@Service
public class WebTextAnalyzer {

  private static Logger        log           = LoggerFactory.getLogger(WebTextAnalyzer.class);

  private static final Pattern hrefPattern   = Pattern.compile(String.format("<a\\s+href=\"([^\\s]+)\""),
                                                               Pattern.CASE_INSENSITIVE);

  private static final Pattern schemePattern = Pattern.compile("http[s]?", Pattern.CASE_INSENSITIVE);

  /**
   * Method to extract a set of links from the HTML page content, constrained to
   * the domain of the request URI
   *
   * @param uri
   * @param pageContent
   * @return
   */
  public static Set<URI> extractLinks(final URI baseURI, final String pageContent, final CrawlOptions options) {
    if (baseURI == null)
      throw new IllegalArgumentException("Base URI must be provided in order to validate extracted links");

    Set<URI> ret = new HashSet<>();
    final Matcher m = hrefPattern.matcher(pageContent);
    while (m.find()) {
      final String target = m.group(1);
      if (StringUtils.isNotBlank(target)) {
        try {
          final String link = URINormalizer.normalize(baseURI.toString(), target);
          if (validate(link, options)) {
            ret.add(URI.create(link));
          }
        }
        catch (IllegalArgumentException iae) {
          log.error("URI syntax error on extracted link target: {}", target);
        }
      }
    } // end while (m.find())

    if (log.isTraceEnabled()) {
      log.trace("Extacted {} links from Base URI '{}'", ret.size(), baseURI.toString());
      if (ret.size() > 0)
        log.trace("==> {}", Arrays.toString(ret.toArray(new String[0])));
    }

    return Collections.unmodifiableSet(ret);
  }

  /**
   * Validates to see whether an extracted link satisfies the following
   * conditions:
   * <ol>
   * <li>Acceptable URI scheme
   * <li>Satisfies URI restriction policy as specified by the
   * {@link CrawlOptions}
   * </ol>
   *
   * @param link
   *          URI to validate
   * @param options
   *          {@link CrawlOptions} for originating web crawl request
   * @return
   */
  protected static boolean validate(final String link, final CrawlOptions options) {
    boolean ret = false;
    try {
      final URI linkURI = new URI(link);
      // validate scheme
      if (linkURI.getScheme() == null || !schemePattern.matcher(linkURI.getScheme()).matches()) {
        log.trace("Skipping Link '{}', failed URI scheme validation", link);
        return false;
      }

      // validate same domain or host
      final URI seedURI = options.seedURI;
      switch (options.restrictionPolicy) {
        case DOMAIN:
          if (!StringUtils.equalsIgnoreCase(URLUtil.getDomainName(linkURI.toString()),
                                            URLUtil.getDomainName(seedURI.toString()))) {
            log.trace("Skipping Link '{}', failed URI domain validation for '{}'", link, seedURI.toString());
            return false;
          }
          break;
        case HOST:
          if (!StringUtils.equalsIgnoreCase(linkURI.getHost(), seedURI.getHost())) {
            log.trace("Skipping Link '{}', failed URI host validation for '{}'", link, seedURI.toString());
            return false;
          }
          break;
      }

      ret = true;
    }
    catch (URISyntaxException | MalformedURLException e) { // shouldn't happen
      log.warn("Unexpected malformed URI link to validate: {}", link);
    }
    return ret;
  }
}
