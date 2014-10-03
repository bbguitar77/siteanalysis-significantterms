package com.bwarner.siteanalysis.crawler.services;

import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwarner.siteanalysis.crawler.exception.HttpException;
import com.bwarner.siteanalysis.crawler.model.HttpResponse;
import com.bwarner.siteanalysis.crawler.model.HttpResponse.HttpResponseBuilder;

public class MockHttpService implements HttpService {

  private static Logger log = LoggerFactory.getLogger(MockHttpService.class);

  @Override
  public HttpResponse doGet(String uri) throws IllegalArgumentException, HttpException {
    if (StringUtils.isBlank(uri))
      throw new IllegalArgumentException("URI string cannot be blank");

    return doGet(URI.create(uri));
  }

  @Override
  public HttpResponse doGet(URI uri) throws IllegalArgumentException, HttpException {
    if (uri == null)
      throw new IllegalArgumentException("URI cannot be null");

    HttpResponseBuilder ret = null;
    final String testFile = String.format("test-data/%s", uriToFilename(uri));
    try {
      final String htmlContent = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(testFile));
      ret = new HttpResponseBuilder().setStatus(200).setContent(htmlContent).setUri(uri);

      // one of our sample test files represents a re-direct to another test
      // data-file
      if (uri.toString().equals("https://stripe.com/features")) {
        ret.setIsRedirect(true).setUri(URI.create("https://stripe.com/us/features"));
      }
    }
    catch (Exception e) { // simulate 404 with unable to locate file
      log.trace("404 Simlulated - Could not open test file '{}'", testFile);
      ret = new HttpResponseBuilder().setStatus(404);
    }

    return ret.build();
  }

  /**
   * Strips HTTP protocol and replaces "/" with "_"
   *
   * @param uri
   * @return
   */
  private static String uriToFilename(final URI uri) {
    return uri.toString().replaceFirst("http[s]?://", "").replaceAll("/", "_");
  }
}
