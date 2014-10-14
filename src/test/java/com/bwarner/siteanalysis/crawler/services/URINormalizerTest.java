package com.bwarner.siteanalysis.crawler.services;

import org.junit.Assert;
import org.junit.Test;

public class URINormalizerTest {

  @Test
  public void normalize() {
    final String expected = "http://stripe.com";
    Assert.assertEquals(expected, URINormalizer.normalize("http://www.stripe.com"));
    Assert.assertEquals(expected, URINormalizer.normalize("https://stripe.com"));
    Assert.assertEquals(expected, URINormalizer.normalize("http://STRIPE.com"));
    Assert.assertEquals(expected, URINormalizer.normalize("http://www.stripe.com/"));
    Assert.assertEquals(expected, URINormalizer.normalize("http://www.stripe.com#"));
    Assert.assertEquals(expected, URINormalizer.normalize("http://www.stripe.com#jumpto"));
    Assert.assertEquals(expected, URINormalizer.normalize("http://www.stripe.com?x=y"));
    Assert.assertEquals(expected, URINormalizer.normalize("http://stripe.com:80"));
  }

  @Test
  public void normalize_nonHttpURI() {
    Assert.assertEquals("mailto:sales@stripe.com", URINormalizer.normalize("mailto:sales@stripe.com"));
  }

  @Test
  public void normalize_IllegalArgs() {
    boolean exceptionCaught = false;
    try {
      URINormalizer.normalize("/about");
    }
    catch (IllegalArgumentException e) {
      exceptionCaught = true;
    }
    Assert.assertTrue("Expected IllegalArgumentException to be thrown", exceptionCaught);
  }

  @Test
  public void normalizeWithBase() {
    Assert.assertEquals("http://stripe.com", URINormalizer.normalize("http://stripe.com", "#"));
    Assert.assertEquals("http://stripe.com", URINormalizer.normalize("http://stripe.com", "?x=y"));
    Assert.assertEquals("http://stripe.com/about", URINormalizer.normalize("https://www.stripe.com/", "/about/"));
    Assert.assertEquals("http://stripe.com/contact.html", URINormalizer.normalize("http://stripe.com", "contact.html"));
    Assert.assertEquals("http://stripe.com/pricing", URINormalizer.normalize("http://stripe.com/api/", "../pricing"));
    Assert.assertEquals("http://stripe.com/pricing", URINormalizer.normalize("http://stripe.com/", "./pricing"));
    Assert.assertEquals("http://stripe.com", URINormalizer.normalize(null, "https://www.stripe.com/"));
  }

  @Test
  public void normalizeWithBase_IllegalArgs() {
    boolean exceptionCaught = false;
    try {
      URINormalizer.normalize("stripe.com", "/x");
    }
    catch (IllegalArgumentException e) {
      exceptionCaught = true;
    }
    Assert.assertTrue("Expected IllegalArgumentException to be thrown", exceptionCaught);
  }
}
