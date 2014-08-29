package com.bwarner.siteanalysis.search.config;

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

  private static Logger log          = LoggerFactory.getLogger(ElasticSearchClientFactory.class);

  protected Node        node;

  protected Client      client;

  /**
   * Should be a path in the classpath.
   */
  protected String      configPath   = "elasticsearch/config";

  /**
   * Should be relative to configPath.
   */
  protected String      templatePath = "templates";

  protected String[]    templates;

  public Client buildClient() throws Exception {
    if (node == null)
      throw new Exception("You must define an ElasticSearch Node.");
    client = node.client();
    initTemplates();
    return client;
  }

  protected void initTemplates() {
    if (null == templates || templates.length == 0)
      return;

    for (String template : templates) {
      try {
        if (isTemplateExist(template)) {
          this.client.admin().indices().prepareDeleteTemplate(template).execute().actionGet();
        }
        pushTemplate(template);
      }
      catch (Exception e) {
        log.error("Unable to load template {}. Error was \"{}\". Skipping.", template, e.getMessage());
        continue;
      }
    }
  }

  private boolean isTemplateExist(String template) {
    GetIndexTemplatesResponse gitr = this.client.admin().indices().prepareGetTemplates(template).execute().actionGet();

    if (gitr.getIndexTemplates().size() > 0)
      return true;
    return false;
  }

  private void pushTemplate(String template) throws IOException {

    String templateFullPath = configPath + "/" + templatePath + "/" + template + ".json";
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templateFullPath);
    if (null == inputStream)
      inputStream = new FileInputStream(templateFullPath);

    // ElasticSearch in-memory nodes require one shard and zero replicas
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
  public void setNode(Node node) {
    this.node = node;
  }

  public void setConfigPath(String configPath) {
    this.configPath = configPath;
  }

  public void setTemplatePath(String templatePath) {
    this.templatePath = templatePath;
  }

  public void setTemplates(String[] templates) {
    this.templates = templates;
  }
}
