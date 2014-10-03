package com.bwarner.siteanalysis.search.services;

import static com.bwarner.siteanalysis.search.services.ElasticSearchIndexingService.DOC_TYPE_SITE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bwarner.siteanalysis.search.model.SiteDocument;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/appContext.siteanalysis.test.xml" })
public class ElasticSearchIndexingServiceTest extends ElasticSearchTestHelper {

  @Autowired
  private ElasticSearchIndexingService esIndexingService;

  @Test
  public void init() {
    Assert.assertNotNull(esIndexingService);
  }

  @Test
  public void indexSites() throws InterruptedException {
    assertFresh(DOC_TYPE_SITE);
    List<SiteDocument> payloads = new ArrayList<>();
    // web sites A-Z
    for (int i = 65; i <= 90; i++) {
      char content[] = new char[100];
      Arrays.fill(content, (char) i);
      payloads.add(new SiteDocument(String.format("http://%s.com", (char) i), new String(content)));
    }
    esIndexingService.indexSites(payloads.toArray(new SiteDocument[0]));
    Thread.sleep(2000); // need to sleep for ES refresh
    Assert.assertEquals("Expected -26- site documents after indexing", 26, documentCount(DOC_TYPE_SITE));
  }
}
