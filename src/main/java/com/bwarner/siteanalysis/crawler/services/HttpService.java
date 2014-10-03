package com.bwarner.siteanalysis.crawler.services;

import java.net.URI;

import com.bwarner.siteanalysis.crawler.exception.HttpException;
import com.bwarner.siteanalysis.crawler.model.HttpResponse;

public interface HttpService {

  /**
   * Executes an HTTP GET request to the given URI
   *
   * @param uri
   *          Request URI String
   * @return {@link HttpResponse} container for information such as
   *         page content, status code, response headers, etc.
   * @throws HttpException
   * @throws IllegalArgumentException
   *           If URI string is blank or not valid
   */
  public HttpResponse doGet(final String uri) throws HttpException, IllegalArgumentException;

  /**
   * Executes an HTTP GET request to the given URI
   *
   * @param uri
   *          Request URI
   * @return {@link HttpResponse} container for information such as
   *         page content, status code, response headers, etc.
   * @throws HttpException
   * @throws IllegalArgumentException
   *           If URI is null
   */
  public HttpResponse doGet(final URI uri) throws HttpException, IllegalArgumentException;
}
