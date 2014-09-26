package com.bwarner.siteanalysis.app;

import java.util.Comparator;
import java.util.concurrent.ExecutionException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bwarner.siteanalysis.integration.model.SiteAnalysisOptions;
import com.bwarner.siteanalysis.integration.model.SiteAnalysisOptions.SiteAnalysisOptionsBuilder;
import com.bwarner.siteanalysis.integration.services.SiteAnalysisService;
import com.bwarner.siteanalysis.search.model.SignificantTermsQueryResponse;
import com.bwarner.siteanalysis.search.model.SignificantTermsQueryResponse.SignificantTerm;
import com.bwarner.siteanalysis.search.services.SearchQueryService;
import com.bwarner.siteanalysis.utils.Utils;

/**
 * Stand-alone script that launches crawling & significant terms aggregation in
 * one launch
 */
public class SignificantTermsAggregator {

  private static Logger              log                              = LoggerFactory.getLogger(SignificantTermsAggregator.class);

  final private static String        SPRING_APP_CONTEXT_RESOURCE_FILE = "classpath:/META-INF/appContext.siteanalysis.standalone.xml";

  /** spring-wired services */
  private static SiteAnalysisService siteAnalysisService;
  private static SearchQueryService  searchQueryService;

  @SuppressWarnings("static-access")
  private static Options buildOptions() {
    Options options = new Options();
    options.addOption(OptionBuilder.withLongOpt("help").withDescription("print this message").create("h"));
    options.addOption(OptionBuilder.withLongOpt("uri")
                                   .withDescription("seed uri to crawl")
                                   .hasArg()
                                   .withArgName("seed uri")
                                   .isRequired()
                                   .create("u"));
    options.addOption(OptionBuilder.withLongOpt("max-depth")
                                   .withDescription("max depth to crawl from origin")
                                   .hasArg()
                                   .withArgName("max depth")
                                   .isRequired()
                                   .create("d"));
    options.addOption(OptionBuilder.withLongOpt("query")
                                   .withDescription("query for significant term aggregation foreground subset")
                                   .hasArg()
                                   .withArgName("query term")
                                   .isRequired()
                                   .create("q"));
    return options;
  }

  private static void printHelp(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setOptionComparator(new Comparator<Option>() {
      @Override
      public int compare(Option opt1, Option opt2) {
        return 1;
      }
    });
    formatter.printHelp("Significant Terms Aggregator", options, true);
  }

  private static void setParameters(CommandLine params) {
    log.info("Program parameters:");
    for (Option o : params.getOptions())
      log.info("- {}: {}", o.getLongOpt(), o.getValue());

    SiteAnalysisOptionsBuilder builder = new SiteAnalysisOptionsBuilder();
    builder.setUri(params.getOptionValue("u"));
    builder.setMaxDepth(Integer.parseInt(params.getOptionValue("d")));

    siteAnalysisOptions = builder.build();
    query = params.getOptionValue("q");
  }

  private static SiteAnalysisOptions siteAnalysisOptions;
  private static String              query;

  public static void main(String[] args) {
    Options options = buildOptions();
    CommandLineParser parser = new GnuParser();
    try {
      CommandLine line = parser.parse(options, args);
      if (line.hasOption("h")) {
        printHelp(options);
        System.exit(0);
      }
      setParameters(line);

      // init spring context
      initApplicationContext();

      // launch analysis execution and block until finished
      siteAnalysisService.analyzeSite(siteAnalysisOptions).get();

      // pause for a bit to allow for ES refresh interval
      Thread.sleep(2000);

      SignificantTermsQueryResponse significantTermsResponse = searchQueryService.getSignificantTerms(query);
      Utils.printLogHeader(log, "Significant Terms Response", new String[] { "query=".concat(query) });
      for (SignificantTerm term : significantTermsResponse.significantTerms) {
        log.info(term.toString());
      }
      log.info("Exiting");
      System.exit(0);
    }
    catch (ParseException e) {
      log.error(e.getMessage());
      printHelp(options);
      System.exit(1);
    }
    catch (ExecutionException ee) {
      log.error("Something went awry during execution of site analysis", ee);
      System.exit(1);
    }
    catch (Exception e) {
      log.error("Significant Terms Aggregation could not be completed. Check log console...", e);
      System.exit(1);
    }
  }

  private static ApplicationContext initApplicationContext() throws BeansException {
    ApplicationContext context = new ClassPathXmlApplicationContext(SPRING_APP_CONTEXT_RESOURCE_FILE);
    siteAnalysisService = context.getBean(SiteAnalysisService.class);
    searchQueryService = context.getBean(SearchQueryService.class);
    return context;
  }
}
