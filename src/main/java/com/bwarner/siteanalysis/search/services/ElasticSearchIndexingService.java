package com.bwarner.siteanalysis.search.services;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bwarner.siteanalysis.search.exception.SearchIndexingException;
import com.bwarner.siteanalysis.search.model.SiteDocument;
import com.bwarner.siteanalysis.utils.Utils;

@Service
public class ElasticSearchIndexingService implements SearchIndexingService {

  private static Logger      log                    = LoggerFactory.getLogger(ElasticSearchIndexingService.class);

  public static final String DOC_TYPE_SITE          = "site";

  public static final String DOC_FIELD_SITE_CONTENT = "content";
  public static final String DOC_FIELD_SITE_DOMAIN  = "domain";
  public static final String DOC_FIELD_SITE_HOST    = "host";
  public static final String DOC_FIELD_SITE_URI     = "uri";

  @Autowired
  private Client             elasticSearchClient;

  @Value("${elasticsearch.indexName}")
  private String             index;

  @Override
  public void indexSites(SiteDocument[] sites) throws SearchIndexingException {
    if (null != sites && sites.length > 0) {
      long indexingStart = System.currentTimeMillis();
      Utils.printLogHeader(log, "Beginning Site Indexing to ES", new String[] { "#-index_requests=" + sites.length });
      BulkRequestBuilder bulkRequest = elasticSearchClient.prepareBulk();
      log.debug("Converting Site payload data to ES indexing requests");
      for (SiteDocument payload : sites) {
        IndexRequestBuilder indexRequest = prepareForIndexing(payload);
        if (null != indexRequest)
          bulkRequest.add(indexRequest);
      }

      // Elastic responds with an error if 0 requests
      if (bulkRequest.numberOfActions() > 0) {
        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
        if (bulkResponse.hasFailures()) {
          triggerError("Failed to index site documents. Reason: " + bulkResponse.buildFailureMessage());
        }
      }
      log.debug("Site data indexing done in {} millis.", (System.currentTimeMillis() - indexingStart));
      Utils.printLogHeader(log, "Done Site Indexing to ES");
    }
    else { // shouldn't happen
      triggerError("No site data to index - something went wrong...");
    }
  }

  protected IndexRequestBuilder prepareForIndexing(SiteDocument payload) {
    IndexRequestBuilder indexRequest = null;
    try {
      final String docId = String.valueOf(payload.uri.hashCode());
      XContentBuilder docFields = jsonBuilder().startObject()
                                               .field(DOC_FIELD_SITE_CONTENT, payload.content)
                                               .field(DOC_FIELD_SITE_DOMAIN, payload.domain)
                                               .field(DOC_FIELD_SITE_HOST, payload.host)
                                               .field(DOC_FIELD_SITE_URI, payload.uri);
      indexRequest = elasticSearchClient.prepareIndex(index, DOC_TYPE_SITE, docId).setSource(docFields);
    }
    catch (IOException ioe) {
      // should never happen...
      log.error("Unable to convert SiteDocument payload to index request. Error was {}", ioe.getMessage());
    }
    return indexRequest;
  }

  private void triggerError(final String errorMsg) throws SearchIndexingException {
    log.error(errorMsg);
    throw new SearchIndexingException(errorMsg);
  }
}
