package com.bwarner.siteanalysis.app;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.bwarner.siteanalysis.app.config.SiteAnalysisAppConfiguration;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.nhuray.dropwizard.spring.SpringBundle;

public class SiteAnalysisApp extends Application<SiteAnalysisAppConfiguration> {

  final private static String SPRING_APP_CONTEXT_RESOURCE_FILE = "classpath:/META-INF/appContext.siteanalysis.standalone.xml";

  public static void main(String[] args) throws Exception {
    new SiteAnalysisApp().run(args);
  }

  @Override
  public String getName() {
    return "site-analysis";
  }

  @Override
  public void initialize(Bootstrap<SiteAnalysisAppConfiguration> bootstrap) {
    // register spring bundle
    bootstrap.addBundle(new SpringBundle<SiteAnalysisAppConfiguration>(applicationContext(), true, false, false));
    // format JSON object mapper
    bootstrap.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    bootstrap.getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
  }

  @Override
  public void run(SiteAnalysisAppConfiguration configuration, Environment environment) {
    // do nothing
  }

  private ConfigurableApplicationContext applicationContext() throws BeansException {
    GenericXmlApplicationContext context = new GenericXmlApplicationContext();
    context.load(SPRING_APP_CONTEXT_RESOURCE_FILE);
    return context;
  }
}
