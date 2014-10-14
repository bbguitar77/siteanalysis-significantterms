package com.bwarner.siteanalysis.crawler.services;

import java.net.URI;
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
import com.bwarner.siteanalysis.crawler.model.CrawlOptions;
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
  public Set<SiteCrawlInfo> crawl(CrawlOptions options) throws CrawlingException {
    try {
      final URI seedURI = options.seedURI;
      Utils.printLogHeader(log,
                           "Beginning Site Crawl",
                           new String[] { "URI=".concat(seedURI.toString()),
                               "max-depth=".concat(String.valueOf(options.maxDepth)) });

      final int maxThreads = Integer.parseInt(crawlThroughput);
      final ExecutorCompletionService<SiteCrawlInfo> crawlerECS = new ExecutorCompletionService<>(Executors.newFixedThreadPool(maxThreads));
      Future<SiteCrawlInfo>[] outgoingFutures = new Future[] { crawlerECS.submit(new SiteCrawlTask(seedURI, 0, options)) };
      Set<SiteCrawlInfo> results = getMore(crawlerECS, Arrays.asList(outgoingFutures), new HashSet<URI>(), options);
      Utils.printLogHeader(log, "Ending Site Crawl", new String[] { "URI=".concat(seedURI.toString()),
          "max-depth=" + options.maxDepth,
          "#-results=" + results.size() });
      return results;
    }
    catch (ExecutionException e) {
      Utils.printLogHeader(log, "Ending Site Crawl [error state]", new String[] { e.getCause().getMessage() });
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
                                       final Set<URI> visitedLinks,
                                       final CrawlOptions options) throws InterruptedException, ExecutionException {

    Set<SiteCrawlInfo> results = new HashSet<>();
    Set<SiteCrawlTask> futureTasks = new HashSet<>();
    for (int i = 0; i < incomingFutures.size(); i++) {
      SiteCrawlInfo crawlInfo = crawlerECS.take().get();
      if (crawlInfo.isSuccess()) {
        log.debug("Successful Crawl: {}", crawlInfo.toString());

        // Check to see if we have already encountered the HTTP response URI
        // (e.g. as a possible re-direct)
        final URI responseURI = crawlInfo.responseData.uri;
        if (visitedLinks.contains(responseURI)) {
          log.debug("[DUPLICATE] HTTP response URI ({}) encountered, omitting response", responseURI);
          // mark original request URI as visited in case of redirect
          visitedLinks.add(crawlInfo.requestURI);
          continue;
        }

        visitedLinks.add(responseURI); // mark as visited
        if (crawlInfo.responseData.isRedirect) {
          // for redirects, mark original request URI as visited
          visitedLinks.add(crawlInfo.requestURI);
        }

        // add response to results
        results.add(crawlInfo);

        // If we have not exceeded the max crawl depth, then add the extracted
        // links to the list of future tasks
        if (crawlInfo.requestDepth < options.maxDepth) {
          for (URI link : crawlInfo.links) {
            SiteCrawlTask ft = new SiteCrawlTask(link, crawlInfo.requestDepth + 1, options);
            futureTasks.add(ft);
          }
        } // end if (info.requestDepth < maxDepth) {
      } // end if (info.isSuccess()) {
      else {
        log.debug("[NON]Successful Crawl: {}", crawlInfo.toString());
        visitedLinks.add(crawlInfo.requestURI); // mark as visited
      }
    } // end for (int i = 0; i < incomingFutures.size(); i++)

    // prune future tasks that have already been visited
    Iterator<SiteCrawlTask> ftIter = futureTasks.iterator();
    while (ftIter.hasNext()) {
      SiteCrawlTask ft = ftIter.next();
      if (visitedLinks.contains(ft.requestURI))
        ftIter.remove();
    } // end while (ftIter.hasNext())

    final boolean keepCrawling = futureTasks.size() > 0;
    if (keepCrawling) {
      final List<Future<SiteCrawlInfo>> outgoingFutures = new ArrayList<>();
      for (SiteCrawlTask task : futureTasks)
        outgoingFutures.add(crawlerECS.submit(task));
      results.addAll(getMore(crawlerECS, outgoingFutures, visitedLinks, options));
    } // end if (keepCrawling)

    return results;
  }
}
