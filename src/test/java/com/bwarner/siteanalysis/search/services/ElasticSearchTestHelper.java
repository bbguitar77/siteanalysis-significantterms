package com.bwarner.siteanalysis.search.services;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.client.Client;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
public class ElasticSearchTestHelper {

  @Autowired
  protected Client elasticSearchClient;

  @Value("${elasticsearch.indexName}")
  protected String index;

  @Before
  public void setup() {
    createIndex();
  }

  @After
  public void teardown() {
    dropIndex();
  }

  protected void createIndex() {
    IndicesExistsResponse indicesExistsResponse = this.elasticSearchClient.admin()
                                                                          .indices()
                                                                          .prepareExists(index)
                                                                          .execute()
                                                                          .actionGet();
    if (!indicesExistsResponse.isExists()) {
      CreateIndexResponse createIndexResponse = this.elasticSearchClient.admin()
                                                                        .indices()
                                                                        .prepareCreate(index)
                                                                        .execute()
                                                                        .actionGet();
      Assert.assertTrue("could not create index", createIndexResponse.isAcknowledged());
    }
  }

  protected void dropIndex() {
    IndicesExistsResponse indicesExistsResponse = this.elasticSearchClient.admin()
                                                                          .indices()
                                                                          .prepareExists(index)
                                                                          .execute()
                                                                          .actionGet();
    if (indicesExistsResponse.isExists()) {
      DeleteIndexResponse deleteIndexResponse = this.elasticSearchClient.admin()
                                                                        .indices()
                                                                        .prepareDelete(index)
                                                                        .execute()
                                                                        .actionGet();
      Assert.assertTrue("could not delete index", deleteIndexResponse.isAcknowledged());
    }
  }

  protected long documentCount(String type) {
    CountResponse countResponse = elasticSearchClient.prepareCount(index).setTypes(type).execute().actionGet();
    return countResponse.getCount();
  }

  protected void assertFresh(String type) {
    Assert.assertEquals("Expected -0- site document count on fresh index", 0, documentCount(type));
  }
}
