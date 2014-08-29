package com.bwarner.siteanalysis.search.config;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * A factory to produce ElasticSearch {@link Node} instances
 *
 * @author bwarner
 */
@Service
public class ElasticSearchNodeFactory {

  private static Logger       log          = LoggerFactory.getLogger(ElasticSearchNodeFactory.class);

  private String              settingsFile;

  private Map<String, String> settings;

  private boolean             local        = false;

  private Set<Node>           managedNodes = new HashSet<>();

  public Node buildNode() {
    ImmutableSettings.Builder settingsBuilder = ImmutableSettings.settingsBuilder().put("node.local", this.local);
    if (StringUtils.isNotBlank(this.settingsFile))
      settingsBuilder.loadFromClasspath(this.settingsFile.trim());
    if (null != settings)
      settingsBuilder.put(settings);

    Node node = NodeBuilder.nodeBuilder().settings(settingsBuilder.build()).node();
    log.debug("Node settings: {}", node.settings().getAsMap());
    managedNodes.add(node);
    return node;
  }

  @PreDestroy
  public void preDestroy() {
    for (Node node : managedNodes) {
      log.debug("Closing client in cluster {}", node.settings().get("cluster.name"));
      node.client().close();
      log.debug("Closing node in cluster {}", node.settings().get("cluster.name"));
      node.close();
    }
  }

  /* GETTERS & SETTERS */
  public void setSettingsFile(String settingsFile) {
    this.settingsFile = settingsFile;
  }

  public void setSettings(Map<String, String> settings) {
    this.settings = settings;
  }

  public void setLocal(boolean local) {
    this.local = local;
  }
}
