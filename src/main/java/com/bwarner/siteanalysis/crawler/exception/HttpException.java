package com.bwarner.siteanalysis.crawler.exception;

public class HttpException extends RuntimeException {

  private static final long serialVersionUID = 1456046697733547464L;

  public HttpException(String message, Throwable cause) {
    super(message, cause);
  }
}
