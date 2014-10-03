package com.bwarner.siteanalysis.crawler.services;

import java.net.URI;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bwarner.siteanalysis.crawler.model.CrawlOptions;
import com.bwarner.siteanalysis.crawler.model.SiteCrawlInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/appContext.siteanalysis.test.xml" })
public class ThreadedSiteCrawlingServiceTest {

  @SuppressWarnings("unused")
  private static Logger               log = LoggerFactory.getLogger(ThreadedSiteCrawlingServiceTest.class);

  @Autowired
  private ThreadedSiteCrawlingService siteCrawlingService;

  @Test
  public void init() {
    Assert.assertNotNull(siteCrawlingService);
  }

  @Test
  public void crawl() {
    Set<SiteCrawlInfo> results = siteCrawlingService.crawl(new CrawlOptions(URI.create("https://stripe.com"), 1));
    // The test data file for stripe.com produces 20 external links, eight of
    // which simulate a 404 and two of which two are duplicate links
    // So, 11 = request URI + 20 extracted links - 8 404s - 2 duplicates
    Assert.assertEquals("Crawling test-data for 'stripe.com' at a depth of one should produce 11 results",
                        11,
                        results.size());
    // Verify results
    for (SiteCrawlInfo siteInfo : results) {
      Assert.assertNotNull("expected non-null request URI", siteInfo.requestURI);
      Assert.assertNotNull("expected non-null HTTP response data", siteInfo.responseData);
      Assert.assertNotNull("expected 200 HTTP status code", siteInfo.responseData.status);
      Assert.assertNotNull("expected non-null HTTP response URI", siteInfo.responseData.uri);
      Assert.assertTrue("expected non-blank HTTP response content",
                        StringUtils.isNotBlank(siteInfo.responseData.content));
      Assert.assertTrue("expected non-empty set of extracted links",
                        siteInfo.links != null && !siteInfo.links.isEmpty());
    }
  }
}
