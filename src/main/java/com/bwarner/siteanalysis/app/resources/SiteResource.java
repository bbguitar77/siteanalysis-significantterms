package com.bwarner.siteanalysis.app.resources;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bwarner.siteanalysis.integration.model.SiteAnalysisOptions.SiteAnalysisOptionsBuilder;
import com.bwarner.siteanalysis.integration.services.SiteAnalysisService;
import com.bwarner.siteanalysis.search.model.SignificantTermsQueryResponse;
import com.bwarner.siteanalysis.search.services.SearchQueryService;
import com.bwarner.siteanalysis.utils.Utils;

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
  public Response crawl(@FormParam("uri") String uri,
                        @FormParam("max-depth") Integer maxDepth,
                        @FormParam("restriction") String restrictionPolicy) throws Exception {
    try {
      SiteAnalysisOptionsBuilder optionsBuilder = new SiteAnalysisOptionsBuilder().setUri(uri);
      if (maxDepth != null)
        optionsBuilder.setMaxDepth(maxDepth);
      if (StringUtils.isNotBlank(restrictionPolicy))
        optionsBuilder.setRestrictionPolicy(restrictionPolicy);
      siteAnalysisService.analyzeSite(optionsBuilder.build());
      return Response.ok("analyzing - check logs").build();
    }
    catch (IllegalArgumentException iae) {
      return buildError(String.format("Malformed parameters: uri=%s, max-depth=%s, restriction=%s",
                                      uri,
                                      maxDepth,
                                      restrictionPolicy), iae);
    }
  }

  @GET
  @Path("/sigterms")
  public Response query(@QueryParam("q") String q) throws Exception {
    SignificantTermsQueryResponse stResponse = searchQueryService.getSignificantTerms(q);
    return Response.ok(stResponse.significantTerms).build();
  }

  /**
   * Builds a 400 HTTP response with the given error message
   */
  protected Response buildError(final String msg, final Exception e) {
    return Response.status(Response.Status.BAD_REQUEST)
                   .entity(String.format("%s\n%s", msg, Utils.getStackTraceText(e)))
                   .type(MediaType.TEXT_PLAIN)
                   .build();
  }
}
