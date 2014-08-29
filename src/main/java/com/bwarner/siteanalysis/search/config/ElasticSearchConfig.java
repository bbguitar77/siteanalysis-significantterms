package com.bwarner.siteanalysis.search.config;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

  @Value("${elasticsearch.node.settingsfile}")
  private String                   nodeSettingsFile;

  @Value("${elasticsearch.node.local}")
  private String                   nodeLocal;

  @Value("${elasticsearch.configPath}")
  private String                   configPath;

  @Value("${elasticsearch.templatePath}")
  private String                   templatePath;

  @Value("${elasticsearch.templates}")
  private String                   templates;

  @Autowired
  private ElasticSearchNodeFactory nodeFactory;

  @Bean(name = "elasticSearchClient")
  public Client elasticSearchClient() throws Exception {

    Client client = null;

    // Build in-memory node
    if (StringUtils.isNotBlank(nodeSettingsFile))
      nodeFactory.setSettingsFile(nodeSettingsFile);
    if (StringUtils.isNotBlank(nodeLocal))
      nodeFactory.setLocal(Boolean.parseBoolean(nodeLocal));
    Node node = nodeFactory.buildNode();

    // Build in-memory Client
    ElasticSearchClientFactory clientFactory = new ElasticSearchClientFactory();
    clientFactory.setNode(node);
    if (StringUtils.isNotBlank(configPath))
      clientFactory.setConfigPath(configPath);
    if (StringUtils.isNotBlank(templatePath))
      clientFactory.setTemplatePath(templatePath);
    if (StringUtils.isNotBlank(templates))
      clientFactory.setTemplates(templates.split("[,]"));

    client = clientFactory.buildClient();
    return client;
  }
}
