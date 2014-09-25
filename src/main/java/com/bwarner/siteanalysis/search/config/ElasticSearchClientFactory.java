package com.bwarner.siteanalysis.search.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchClientFactory {

  private static Logger       log          = LoggerFactory.getLogger(ElasticSearchClientFactory.class);

  private Node                node;

  private String[]            templates;

  /** should be present in classpath */
  private static final String configPath   = "elasticsearch" + File.separator + "config";

  /** relative to configPath */
  private static final String templatePath = "templates";

  public Client buildClient() throws Exception {
    if (node == null)
      throw new RuntimeException("You must define an ElasticSearch Node.");
    Client client = node.client();
    initTemplates(client);
    return client;
  }

  protected void initTemplates(final Client client) {
    if (null == templates || templates.length == 0)
      return;

    for (String template : templates) {
      try {
        if (isTemplateDefined(client, template)) {
          client.admin().indices().prepareDeleteTemplate(template).execute().actionGet();
        }
        pushTemplate(client, template);
      }
      catch (Exception e) {
        log.error("Unable to load template {}. Error was \"{}\". Skipping.", template, e.getMessage());
        continue;
      }
    }
  }

  private boolean isTemplateDefined(final Client client, final String template) {
    GetIndexTemplatesResponse gitr = client.admin().indices().prepareGetTemplates(template).execute().actionGet();
    if (gitr.getIndexTemplates().size() > 0)
      return true;
    return false;
  }

  private void pushTemplate(final Client client, final String template) throws IOException {
    String templateFullPath = configPath + File.separator + templatePath + File.separator + template + ".json";
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templateFullPath);
    if (null == inputStream)
      inputStream = new FileInputStream(templateFullPath);

    String templateJson = IOUtils.toString(inputStream);
    PutIndexTemplateResponse templateResponse = client.admin()
                                                      .indices()
                                                      .preparePutTemplate(template)
                                                      .setSource(templateJson)
                                                      .execute()
                                                      .actionGet();
    if (!templateResponse.isAcknowledged())
      throw new IOException("Unable to push template " + template + " to node.");
  }

  /* GETTERS & SETTERS */
  public ElasticSearchClientFactory setNode(Node node) {
    this.node = node;
    return this;
  }

  public ElasticSearchClientFactory setTemplates(String[] templates) {
    this.templates = templates;
    return this;
  }
}
