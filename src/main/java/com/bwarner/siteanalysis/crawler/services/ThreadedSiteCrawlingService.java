package com.bwarner.siteanalysis.crawler.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bwarner.siteanalysis.crawler.exception.CrawlingException;
import com.bwarner.siteanalysis.crawler.model.SiteCrawlInfo;
import com.bwarner.siteanalysis.utils.Utils;

@Service
public class ThreadedSiteCrawlingService implements SiteCrawlingService {

  private static Logger log = LoggerFactory.getLogger(ThreadedSiteCrawlingService.class);

  @Value("${siteanalysis.crawler.throughput:5}")
  private String        crawlThroughput;

  @Autowired
  private HttpService   httpService;

  @PostConstruct
  public void init() {
    SiteCrawlTask.httpService = httpService;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<SiteCrawlInfo> crawl(final String uri, final int maxDepth) throws CrawlingException {
    try {
      Utils.printLogHeader(log,
                           "Beginning Site Crawl",
                           new String[] { "URI=".concat(uri), "max-depth=".concat(String.valueOf(maxDepth)) });

      final int maxThreads = Integer.parseInt(crawlThroughput);
      final ExecutorCompletionService<SiteCrawlInfo> crawlerECS = new ExecutorCompletionService<>(Executors.newFixedThreadPool(maxThreads));
      Future<SiteCrawlInfo>[] outgoingFutures = new Future[] { crawlerECS.submit(new SiteCrawlTask(uri, 0)) };
      Set<SiteCrawlInfo> results = getMore(crawlerECS, Arrays.asList(outgoingFutures), new HashSet<String>(), maxDepth);
      Utils.printLogHeader(log, "Ending Site Crawl", new String[] { "URI=".concat(uri),
          "max-depth=" + maxDepth,
          "#-results=" + results.size() });
      return results;
    }
    catch (ExecutionException e) {
      Utils.printLogHeader(log, "Ending Site Crawl [error state]", new String[] { e.getMessage() });
      throw new CrawlingException("ExecutionException triggered by Executor completion service", e);
    }
    catch (Exception e) {
      Utils.printLogHeader(log, "Ending Site Crawl [error state]", new String[] { e.getMessage() });
      throw new CrawlingException("Unknown error triggered during crawling", e);
    }
  }

  protected Set<SiteCrawlInfo> getMore(final ExecutorCompletionService<SiteCrawlInfo> crawlerECS,
                                       final List<Future<SiteCrawlInfo>> incomingFutures,
                                       final Set<String> visitedLinks,
                                       final int maxDepth) throws InterruptedException, ExecutionException {

    Set<SiteCrawlInfo> siteData = new HashSet<>();
    List<SiteCrawlTask> futureTasks = new ArrayList<>();
    for (int i = 0; i < incomingFutures.size(); i++) {
      SiteCrawlInfo info = crawlerECS.take().get();
      if (info.isSuccess()) {
        log.debug("Successful Site crawling response: {}", info.toString());
        // Check to see if we have already encountered the HTTP response URI
        // (e.g. as a possible re-direct)
        final String responseUri = info.httpResponse.uri;
        if (visitedLinks.contains(responseUri)) {
          log.debug("Omitting crawl response - the HTTP response URI [{}] has already been encountered", responseUri);
          continue;
        }

        // Add the request URI (plus redirect URI, if applicable) to the set
        // of visited links
        visitedLinks.add(info.requestUri);
        if (info.httpResponse.isRedirect)
          visitedLinks.add(info.httpResponse.uri);

        // Add crawl info to result set
        siteData.add(info);

        // If we have not exceeded the max crawl depth, then add the extracted
        // links to the list of future tasks
        if (info.requestDepth < maxDepth) {
          for (String link : info.extractedLinks) {
            // only create a future task if we haven't encountered the link
            // before
            if (!visitedLinks.contains(link)) {
              futureTasks.add(new SiteCrawlTask(link, info.requestDepth + 1));
              visitedLinks.add(link);
            }
          }
        } // end if (info.requestDepth < maxDepth) {
      } // end if (info.isSuccess()) {
      else {
        log.warn("NON-successful Site crawling response: {}", info.toString());
      }
    }

    final boolean keepCrawling = futureTasks.size() > 0;
    if (keepCrawling) {
      final List<Future<SiteCrawlInfo>> outgoingFutures = new ArrayList<>();
      for (SiteCrawlTask task : futureTasks)
        outgoingFutures.add(crawlerECS.submit(task));
      siteData.addAll(getMore(crawlerECS, outgoingFutures, visitedLinks, maxDepth));
    }

    return siteData;
  }
}
