package com.bwarner.siteanalysis.search.config;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

  @Value("${elasticsearch.node.settingsfile}")
  private String nodeSettingsFile;

  @Value("${elasticsearch.templates}")
  private String templates;

  @Bean(name = "elasticSearchClient")
  public Client elasticSearchClient() throws Exception {
    // Build in-memory node
    Node node = new ElasticSearchNodeFactory().setSettingsFile(nodeSettingsFile).setLocal(true).buildNode();

    // Build client
    ElasticSearchClientFactory clientFactory = new ElasticSearchClientFactory().setNode(node);
    if (StringUtils.isNotBlank(templates))
      clientFactory.setTemplates(templates.split("[,]"));
    return clientFactory.buildClient();
  }
}
