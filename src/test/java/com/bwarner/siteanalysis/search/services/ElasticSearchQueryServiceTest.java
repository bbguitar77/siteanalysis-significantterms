package com.bwarner.siteanalysis.search.services;

import static com.bwarner.siteanalysis.search.services.ElasticSearchIndexingService.DOC_TYPE_SITE;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bwarner.siteanalysis.search.model.SignificantTermsQueryResponse;
import com.bwarner.siteanalysis.search.model.SignificantTermsQueryResponse.SignificantTerm;
import com.bwarner.siteanalysis.search.model.SiteDocument;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/appContext.siteanalysis.test.xml" })
public class ElasticSearchQueryServiceTest extends ElasticSearchTestHelper {

  @Autowired
  private ElasticSearchIndexingService esIndexingService;

  @Autowired
  private ElasticSearchQueryService    esQueryService;

  @Test
  public void getSignificantTerms() throws InterruptedException {
    assertFresh(DOC_TYPE_SITE);

    // setup some sample data
    SiteDocument[] siteData = generateSiteData();
    esIndexingService.indexSites(siteData);
    Thread.sleep(1000); // need to sleep for ES refresh
    Assert.assertEquals("Expected -5- site documents after indexing", 5, documentCount(DOC_TYPE_SITE));

    /*
     * Really hard to generate "meaningful" significant terms from a low sample
     * of test data, but at least we can verify the response data structure
     */
    SignificantTermsQueryResponse resp = esQueryService.getSignificantTerms("ipsum");
    Assert.assertEquals("Unexpected number of significant terms", 5, resp.significantTerms.size());
    for (SignificantTerm significantTerm : resp.significantTerms) {
      Assert.assertTrue("Expected non-blank sig-term key", StringUtils.isNotBlank(significantTerm.key));
      Assert.assertTrue("Expected non-zero sig-term score", significantTerm.significanceScore > 0);
      Assert.assertTrue("Expected non-zero sig-term frequency", significantTerm.hits > 0);
    }
  }

  private static SiteDocument[] generateSiteData() {
    return new SiteDocument[] { new SiteDocument("http://lorem.com",
                                                 "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum"),
        new SiteDocument("http://perspiciatis.com",
                         "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem"),
        new SiteDocument("http://minima.com",
                         "Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur"),
        new SiteDocument("http://accusamus.com",
                         "At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus, omnis voluptas assumenda est, omnis dolor repellendus"),
        new SiteDocument("http://Temporibus.com",
                         "Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat."), };

  }
}
