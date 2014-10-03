package com.bwarner.siteanalysis.app.resources;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

  @POST
  @Path("/analyze")
  public Response crawl(@FormParam("uri") String uri, @FormParam("max-depth") Integer maxDepth) throws Exception {
    try {
      SiteAnalysisOptionsBuilder options = new SiteAnalysisOptionsBuilder().setUri(uri);
      if (maxDepth != null)
        options.setMaxDepth(maxDepth);
      siteAnalysisService.analyzeSite(options.build());
      return Response.ok("analyzing - check logs").build();
    }
    catch (IllegalArgumentException iae) {
      return buildError("Malformed parameters", iae);
    }
  }

  @GET
  @Path("/sigterms")
  public Response query(@QueryParam("qt") String qt) throws Exception {
    SignificantTermsQueryResponse stResponse = searchQueryService.getSignificantTerms(qt);
    return Response.ok(stResponse.significantTerms).build();
  }

  /**
   * Builds a 400 HTTP response with the given error message
   */
  protected Response buildError(final String msg, final Exception e) {
    return Response.status(Response.Status.BAD_REQUEST)
                   .entity(String.format("%s: %s", msg, e.getMessage()))
                   .type(MediaType.TEXT_PLAIN)
                   .build();
  }
}
