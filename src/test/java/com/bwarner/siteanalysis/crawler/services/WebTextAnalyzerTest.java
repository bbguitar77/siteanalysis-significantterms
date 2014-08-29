package com.bwarner.siteanalysis.crawler.services;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebTextAnalyzerTest {

  private static Logger log = LoggerFactory.getLogger(WebTextAnalyzerTest.class);

  @Test
  public void extractLinks() {
    final String testFile = "test-data/stripe.com";
    try {
      String htmlContent = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(testFile));
      Assert.assertTrue("Expected non-blank HTML content to analyze", StringUtils.isNotBlank(htmlContent));

      Set<String> links = WebTextAnalyzer.extractLinks("https://stripe.com", htmlContent);
      Assert.assertNotNull("Expected non-null set of extracted links", links);
      Assert.assertEquals("Wrong number of extracted links from text", 20, links.size());
      for (String link : links)
        Assert.assertTrue(String.format("Link '%s' is not applicable to the crawled domain", link),
                          link.matches("^http[s]?://(www.)?stripe.com(/.*)?"));
    }
    catch (IOException e) {
      log.error("Could not open test file '{}'", testFile);
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void validateLink() {
    final String domain = "stripe.com";
    Assert.assertTrue("Expected link to be validated", WebTextAnalyzer.validateLink(domain, "http://stripe.com"));
    Assert.assertTrue("Expected link to be validated", WebTextAnalyzer.validateLink(domain, "https://stripe.com"));
    Assert.assertTrue("Expected link to be validated", WebTextAnalyzer.validateLink(domain, "/about"));
    Assert.assertFalse("Expected link to be NOT validated",
                       WebTextAnalyzer.validateLink(domain, "http://blog.stripe.com"));
    Assert.assertFalse("Expected link to be NOT validated", WebTextAnalyzer.validateLink(domain, "feed://stripe"));
  }
}
