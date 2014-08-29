package com.bwarner.siteanalysis.search.services;

import static com.bwarner.siteanalysis.search.services.ElasticSearchIndexingService.DOC_FIELD_SITE_CONTENT;
import static com.bwarner.siteanalysis.search.services.ElasticSearchIndexingService.DOC_TYPE_SITE;
import static org.elasticsearch.index.query.QueryBuilders.queryString;
import static org.elasticsearch.search.aggregations.AggregationBuilders.significantTerms;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchException;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bwarner.siteanalysis.search.exception.SearchQueryException;
import com.bwarner.siteanalysis.search.model.SignificantTermsQueryResponse;

@Service
public class ElasticSearchQueryService implements SearchQueryService {

  private static Logger log = LoggerFactory.getLogger(ElasticSearchQueryService.class);

  @Autowired
  private Client        elasticSearchClient;

  @Value("${siteanalysis.query.significant-terms.mindocs:3}")
  private String        significantTerms_minDocCount;

  @Value("${siteanalysis.query.significant-terms.size:3}")
  private String        significantTerms_Size;

  @Value("${elasticsearch.indexName}")
  private String        index;

  @Override
  public SignificantTermsQueryResponse getSignificantTerms(final String query) throws SearchQueryException {
    try {
      long requestStart = System.currentTimeMillis();

      SearchRequestBuilder request = elasticSearchClient.prepareSearch(index).setTypes(DOC_TYPE_SITE);
      request.setQuery(queryString(query));
      // don't care about search hits, just the aggregation results
      request.setSize(0);
      request.addAggregation(significantTerms("site_content").field(DOC_FIELD_SITE_CONTENT)
                                                             .minDocCount(Integer.parseInt(significantTerms_minDocCount))
                                                             .size(Integer.parseInt(significantTerms_Size)));
      log.trace("{}", request.toString());

      log.debug("Executing ES request with significant terms aggregation for query '{}'", query);
      SearchResponse searchResponse = request.execute().actionGet();
      log.debug("Results received in {} millis.", (System.currentTimeMillis() - requestStart));
      log.trace("{}", searchResponse);

      List<SignificantTermsQueryResponse.SignificantTerm> significantTerms = new ArrayList<>();
      for (Aggregation agg : searchResponse.getAggregations()) {
        SignificantTerms sigTermsAgg = (SignificantTerms) agg;
        for (Bucket bucket : sigTermsAgg.getBuckets()) {
          significantTerms.add(new SignificantTermsQueryResponse.SignificantTerm(bucket.getKey(),
                                                                                 bucket.getSignificanceScore(),
                                                                                 bucket.getDocCount(),
                                                                                 bucket.getSubsetSize(),
                                                                                 bucket.getSupersetSize()));
        }
      }
      return new SignificantTermsQueryResponse(significantTerms);
    }
    catch (SearchException se) {
      final String errorMsg = "Could not complete ElasticSearch query for significant-terms at this time. Reason: " + se.getDetailedMessage();
      log.error(errorMsg);
      throw new SearchQueryException(errorMsg, se);
    }
  }
}
