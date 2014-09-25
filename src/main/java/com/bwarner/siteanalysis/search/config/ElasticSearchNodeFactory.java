package com.bwarner.siteanalysis.search.config;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ElasticSearchNodeFactory {

  private static Logger    log    = LoggerFactory.getLogger(ElasticSearchNodeFactory.class);

  private String           settingsFile;

  private boolean          local;

  private static Set<Node> _nodes = new HashSet<>();

  public Node buildNode() {
    ImmutableSettings.Builder settingsBuilder = ImmutableSettings.settingsBuilder().put("node.local", local);
    if (StringUtils.isNotBlank(this.settingsFile))
      settingsBuilder.loadFromClasspath(this.settingsFile.trim());

    Node node = NodeBuilder.nodeBuilder().settings(settingsBuilder.build()).node();
    _nodes.add(node);
    log.debug("Node settings: {}", node.settings().getAsMap());
    return node;
  }

  @PreDestroy
  public void preDestroy() {
    for (Node node : _nodes) {
      log.debug("Closing client in cluster {}", node.settings().get("cluster.name"));
      node.client().close();
      log.debug("Closing node in cluster {}", node.settings().get("cluster.name"));
      node.close();
    }
  }

  /* GETTERS & SETTERS */
  public ElasticSearchNodeFactory setSettingsFile(String settingsFile) {
    this.settingsFile = settingsFile;
    return this;
  }

  public ElasticSearchNodeFactory setLocal(boolean local) {
    this.local = local;
    return this;
  }
}
