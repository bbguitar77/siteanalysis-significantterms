package com.bwarner.siteanalysis.crawler.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
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

  /**
   * Recursive function that processes a set of incoming {@link SiteCrawlInfo}
   * future results (blocking until all are completed) for a particular crawl
   * depth.
   *
   * <p>
   * Recursive loop is broken once the max-depth is reached
   *
   * @param crawlerECS
   * @param incomingFutures
   * @param visitedLinks
   * @param maxDepth
   * @return
   * @throws InterruptedException
   * @throws ExecutionException
   */
  protected Set<SiteCrawlInfo> getMore(final ExecutorCompletionService<SiteCrawlInfo> crawlerECS,
                                       final List<Future<SiteCrawlInfo>> incomingFutures,
                                       final Set<String> visitedLinks,
                                       final int maxDepth) throws InterruptedException, ExecutionException {

    Set<SiteCrawlInfo> siteData = new HashSet<>();
    Set<SiteCrawlTask> futureTasks = new HashSet<>();
    for (int i = 0; i < incomingFutures.size(); i++) {
      SiteCrawlInfo info = crawlerECS.take().get();
      if (info.isSuccess()) {
        log.debug("Successful Crawl: {}", info.toString());

        // Check to see if we have already encountered the HTTP response URI
        // (e.g. as a possible re-direct)
        final String responseUri = info.httpResponse.uri;
        if (visitedLinks.contains(responseUri)) {
          log.debug("Duplicate HTTP response URI ({}) encountered, omitting response", responseUri);
          continue;
        }

        visitedLinks.add(responseUri); // mark as visited
        if (info.httpResponse.isRedirect) {
          // for redirects, mark original request URI as visited
          visitedLinks.add(info.requestUri);
        }

        // add response to results
        siteData.add(info);

        // If we have not exceeded the max crawl depth, then add the extracted
        // links to the list of future tasks
        if (info.requestDepth < maxDepth) {
          for (String link : info.extractedLinks) {
            SiteCrawlTask ft = new SiteCrawlTask(link, info.requestDepth + 1);
            futureTasks.add(ft);
          }
        } // end if (info.requestDepth < maxDepth) {
      } // end if (info.isSuccess()) {
      else {
        log.debug("[NON]Successful Crawl: {}", info.toString());
        visitedLinks.add(info.requestUri); // mark as visited
      }
    } // end for (int i = 0; i < incomingFutures.size(); i++)

    // prune future tasks that have already been visited
    Iterator<SiteCrawlTask> ftIter = futureTasks.iterator();
    while (ftIter.hasNext()) {
      SiteCrawlTask ft = ftIter.next();
      if (visitedLinks.contains(ft.uri))
        ftIter.remove();
    } // end while (ftIter.hasNext()) {

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
