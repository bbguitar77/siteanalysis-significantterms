package com.bwarner.siteanalysis.crawler.services;

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

public class URINormalizerTest {

  @Test
  public void normalize() throws URISyntaxException {
    final String expected = "http://stripe.com";
    Assert.assertEquals(expected, URINormalizer.normalize("stripe.com"));
    Assert.assertEquals(expected, URINormalizer.normalize("stripe.com/"));
    Assert.assertEquals(expected, URINormalizer.normalize("stripe.com?"));
    Assert.assertEquals(expected, URINormalizer.normalize("STRIPE.com"));
    Assert.assertEquals(expected, URINormalizer.normalize("www.stripe.com"));
    Assert.assertEquals(expected, URINormalizer.normalize("http://stripe.com:80"));
    Assert.assertEquals(expected, URINormalizer.normalize("www.stripe.com?x=y"));
    Assert.assertEquals(expected, URINormalizer.normalize("www.stripe.com#jumpto"));
  }
}
