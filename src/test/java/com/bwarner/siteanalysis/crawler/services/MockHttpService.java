package com.bwarner.siteanalysis.crawler.services;

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
      throw new IllegalArgumentException("URI parameter cannot be blank");

    HttpResponseBuilder ret = null;
    final String testFile = String.format("test-data/%s", uriToFilename(uri));
    try {
      final String htmlContent = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(testFile));
      ret = new HttpResponseBuilder().setStatus(200).setContent(htmlContent);

      // one of our sample test files represents a re-direct to another test
      // data-file
      if (uri.equals("https://stripe.com/features")) {
        ret.setIsRedirect(true).setUri("https://stripe.com/us/features");
      }
    }
    catch (Exception e) { // simulate 404 with unable to locate file
      log.trace("404 Simlulated - Could not open test file '{}'", testFile);
      ret = new HttpResponseBuilder().setStatus(500);
    }

    return ret.build();
  }

  /**
   * Strips HTTP protocol and replaces "/" with "_"
   *
   * @param uri
   * @return
   */
  private static String uriToFilename(final String uri) {
    return uri.replaceFirst("http[s]?://", "").replaceAll("/", "_");
  }
}
