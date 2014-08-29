package com.bwarner.siteanalysis.crawler.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bwarner.siteanalysis.crawler.exception.HttpException;
import com.bwarner.siteanalysis.crawler.model.HttpResponse;
import com.bwarner.siteanalysis.crawler.model.HttpResponse.HttpResponseBuilder;

/**
 * {@link HttpService} implementation utilizing Apache's HttpComponents library
 *
 * @author bwarner
 */
@Service
public class ApacheHttpService implements HttpService {

  private static final Logger log = LoggerFactory.getLogger(ApacheHttpService.class);

  @Override
  public HttpResponse doGet(final String uri) throws IllegalArgumentException, HttpException {
    if (StringUtils.isBlank(uri))
      throw new IllegalArgumentException("URI parameter cannot be blank");

    CloseableHttpClient httpClient = null;
    CloseableHttpResponse response = null;
    HttpResponseBuilder responseBuilder = new HttpResponseBuilder().setUri(uri);
    try {
      try {
        // create default HttpClient - connection & socket timeout(s) set to 10s
        httpClient = defaultHttpClient(true);
        HttpClientContext httpContext = HttpClientContext.create();

        // create HttpGet instance
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setConfig(defaultRequestConfig());

        // execute request
        response = httpClient.execute(httpGet, httpContext);
        checkHttpRedirect(httpContext, httpGet, responseBuilder);
      }
      catch (Exception e) {
        final String errorMsg = String.format("Could not make HTTP request to URI '%s'. Reason: %s",
                                              uri,
                                              e.getMessage());
        log.error(errorMsg);
        throw new HttpException(errorMsg, e);
      }

      handleHttpResponse(response, responseBuilder);
    }
    finally {
      if (null != response) {
        try {
          response.close();
        }
        catch (IOException Warn) {
          log.warn("Could not close HttpResponse object", Warn);
        }
      }
      if (null != httpClient) {
        try {
          httpClient.close();
        }
        catch (IOException Warn) {
          log.warn("Could not close HttpClient object", Warn);
        }
      }
    }
    return responseBuilder.build();
  }

  /**
   * Creates a closeable HttpClient with some default configurations
   *
   * @param retry
   *          If <code>true</code>, then retry attempts set to one
   * @return
   */
  private static CloseableHttpClient defaultHttpClient(final boolean retry) {
    return HttpClients.custom()
                      .setDefaultRequestConfig(defaultRequestConfig())
                      .setRetryHandler(new DefaultHttpRequestRetryHandler((retry ? 1 : 0), false))
                      .build();
  }

  /**
   * Quick note about {@link CookieSpecs.IGNORE_COOKIES} - Some sites return
   * cookies that are not compliant with certain specifications, so it's best to
   * not have the HttpClient validate them
   */
  private static RequestConfig defaultRequestConfig() {
    return RequestConfig.custom()
                        .setConnectTimeout(10000)
                        .setSocketTimeout(10000)
                        .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                        .setRedirectsEnabled(true)
                        .build();
  }

  private static void checkHttpRedirect(HttpClientContext httpContext,
                                        HttpGet httpGet,
                                        final HttpResponseBuilder responseBuilder) {
    String requestUri = StringUtils.trim(httpGet.getURI().toString());
    try {
      URI resolvableLocation = URIUtils.resolve(httpGet.getURI(),
                                                httpContext.getTargetHost(),
                                                httpContext.getRedirectLocations());
      if (null != resolvableLocation && !resolvableLocation.toString().equalsIgnoreCase(requestUri)) {
        responseBuilder.setIsRedirect(true).setUri(resolvableLocation.toString());
      }
    }

    catch (URISyntaxException e) {
      log.error("Error checking HTTP re-direct info for URL {}", requestUri);
    }
  }

  private static void
      handleHttpResponse(final CloseableHttpResponse response, final HttpResponseBuilder responseBuilder) throws HttpException {

    responseBuilder.setStatus(response.getStatusLine().getStatusCode());
    responseBuilder.setStatusText(response.getStatusLine().getReasonPhrase());
    // Populate headers
    if (null != response.getAllHeaders()) {
      Map<String, String> headers = new HashMap<String, String>();
      for (Header header : response.getAllHeaders()) {
        headers.put(header.getName(), header.getValue());
      }
      responseBuilder.setResponseHeaders(headers);
    }

    final HttpEntity entity = response.getEntity();
    if (null != entity) {
      try {
        ContentType cType = ContentType.get(entity);
        if (null != cType && null != cType.getCharset())
          responseBuilder.setCharset(cType.getCharset().toString());
        else
          responseBuilder.setCharset("UTF-8");

        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(),
                                                                         responseBuilder.getCharset()));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
          builder.append(line);
        responseBuilder.setContent(builder.toString());
      }
      catch (Exception e) {
        final String errorMsg = "Unable to parse contents of '" + responseBuilder.getUri() + "': " + e.getMessage();
        log.error(errorMsg);
        throw new HttpException(errorMsg, e);
      }
    }
  }
}
