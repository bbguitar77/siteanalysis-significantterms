package com.bwarner.siteanalysis.crawler.services;

import com.bwarner.siteanalysis.crawler.exception.HttpException;
import com.bwarner.siteanalysis.crawler.model.HttpResponse;

public interface HttpService {

  /**
   * Executes an HTTP GET request to the given URI
   *
   * @param uri
   *          Request URI
   * @return {@link HttpResponse} object containing information such as
   *         pageContent, status code, response headers, etc.
   * @throws HttpException
   * @throws IllegalArgumentException
   */
  public HttpResponse doGet(final String uri) throws HttpException, IllegalArgumentException;
}
