package com.bwarner.siteanalysis.integration.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bwarner.siteanalysis.crawler.model.SiteCrawlInfo;
import com.bwarner.siteanalysis.search.model.SiteDocument;

@Service
public class SiteDocumentTransformer implements Transformer<SiteDocument> {

  private static Logger log = LoggerFactory.getLogger(SiteDocumentTransformer.class);

  @Override
  public SiteDocument[] transform(SiteCrawlInfo[] items) {
    log.debug("Transforming {} SiteCrawlInfo items to SiteDocuments");
    List<SiteDocument> ret = new ArrayList<>();
    for (SiteCrawlInfo item : items)
      ret.add(new SiteDocument(item.httpResponse.uri, item.httpResponse.content));
    return ret.toArray(new SiteDocument[0]);
  }
}
