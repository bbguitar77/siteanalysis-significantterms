package com.bwarner.siteanalysis.app.resources;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bwarner.siteanalysis.integration.model.SiteAnalysisOptions.SiteAnalysisOptionsBuilder;
import com.bwarner.siteanalysis.integration.services.SiteAnalysisService;
import com.bwarner.siteanalysis.search.model.SignificantTermsQueryResponse;
import com.bwarner.siteanalysis.search.services.SearchQueryService;

@Component
@Path("/site")
@Produces(MediaType.APPLICATION_JSON)
public class SiteResource {

  @Autowired
  private SiteAnalysisService siteAnalysisService;

  @Autowired
  private SearchQueryService  searchQueryService;

  @GET
  @Path("/analyze")
  public Response
      crawl(@QueryParam("url") String url, @DefaultValue("2") @QueryParam("max-depth") Integer maxDepth) throws Exception {
    siteAnalysisService.analyzeSite(new SiteAnalysisOptionsBuilder().setUri(url).setMaxDepth(maxDepth).build());
    return Response.ok("analyzing - check logs").build();
  }

  @GET
  @Path("/query")
  public Response query(@QueryParam("q") String query) throws Exception {
    SignificantTermsQueryResponse stResponse = searchQueryService.getSignificantTerms(query);
    return Response.ok(stResponse.significantTerms).build();
  }
}
