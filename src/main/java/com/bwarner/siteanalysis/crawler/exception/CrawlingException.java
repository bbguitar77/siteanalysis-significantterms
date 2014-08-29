package com.bwarner.siteanalysis.crawler.exception;

public class CrawlingException extends RuntimeException {

  private static final long serialVersionUID = -3004665730658320828L;

  public CrawlingException(String message, Throwable cause) {
    super(message, cause);
  }
}
