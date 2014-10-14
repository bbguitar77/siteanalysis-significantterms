package com.bwarner.siteanalysis.crawler.services;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwarner.siteanalysis.crawler.model.CrawlOptions;
import com.bwarner.siteanalysis.crawler.model.CrawlOptions.RestrictionPolicy;

public class WebTextAnalyzerTest {

  private static Logger log = LoggerFactory.getLogger(WebTextAnalyzerTest.class);

  @Test
  public void extractLinks() {
    final String testFile = "test-data/stripe.com";
    try {
      String htmlContent = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(testFile));
      Assert.assertTrue("Expected non-blank HTML content to analyze", StringUtils.isNotBlank(htmlContent));

      final CrawlOptions crawlOptions = new CrawlOptions("http://stripe.com", 0);
      Set<URI> links = WebTextAnalyzer.extractLinks(URI.create("https://stripe.com"), htmlContent, crawlOptions);
      Assert.assertNotNull("Expected non-null set of extracted links", links);
      Assert.assertEquals("Wrong number of extracted links from text", 20, links.size());
      for (URI link : links)
        Assert.assertTrue(String.format("Link '%s' is not applicable to the crawled domain", link),
                          link.toString().matches("^http[s]?://(www.)?stripe.com(/.*)?"));
    }
    catch (IOException e) {
      log.error("Could not open test file '{}'", testFile);
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void validateLink_restrictionHost() {
    final CrawlOptions crawlOptions = new CrawlOptions("http://stripe.com", 0);
    Assert.assertTrue("Expected link to be validated", WebTextAnalyzer.validate("http://stripe.com", crawlOptions));
    Assert.assertTrue("Expected link to be validated", WebTextAnalyzer.validate("https://stripe.com", crawlOptions));
    Assert.assertFalse("Expected link to be NOT validated",
                       WebTextAnalyzer.validate("http://blog.stripe.com", crawlOptions));
    Assert.assertFalse("Expected link to be NOT validated", WebTextAnalyzer.validate("feed://stripe.com", crawlOptions));
  }

  @Test
  public void validateLink_restrictionDomain() {
    final CrawlOptions crawlOptions = new CrawlOptions(URI.create("http://stripe.com"), 0, RestrictionPolicy.DOMAIN);
    Assert.assertTrue("Expected link to be validated", WebTextAnalyzer.validate("http://blog.stripe.com", crawlOptions));
    Assert.assertTrue("Expected link to be validated",
                      WebTextAnalyzer.validate("https://payments.stripe.com", crawlOptions));
    Assert.assertFalse("Expected link to be NOT validated", WebTextAnalyzer.validate("feed://stripe.com", crawlOptions));
  }
}
