package com.bwarner.siteanalysis.crawler.model;

import java.net.URI;
import java.util.Map;

/**
 * Encapsulates various HTTP response information
 */
public class HttpResponse {

  public final String              content;
  public final String              charset;
  public final Integer             status;
  public final String              statusText;
  public final URI                 uri;
  public final Boolean             isRedirect;
  public final Map<String, String> responseHeaders;

  private HttpResponse(HttpResponseBuilder builder) {
    this.content = builder.getContent();
    this.charset = builder.getCharset();
    this.status = builder.getStatus();
    this.statusText = builder.getStatusText();
    this.uri = builder.getUri();
    this.isRedirect = builder.isRedirect();
    this.responseHeaders = builder.getResponseHeaders();
  }

  @Override
  public String toString() {
    return String.format("Status: [%s], Content Length: [%s], Redirect: [%s]",
                         (null != status ? status : "n/a"),
                         (null != content ? content.length() : "n/a"),
                         (isRedirect ? uri : "none"));
  }

  public static class HttpResponseBuilder {
    private String              content;
    private String              charset;
    private Integer             status;
    private String              statusText;
    private URI                 uri;
    private Boolean             isRedirect = false;
    private Map<String, String> responseHeaders;

    public HttpResponseBuilder() {
    }

    public HttpResponse build() {
      return new HttpResponse(this);
    }

    /* GETTERS & SETTERS */
    public String getContent() {
      return content;
    }

    public HttpResponseBuilder setContent(String content) {
      this.content = content;
      return this;
    }

    public String getCharset() {
      return charset;
    }

    public HttpResponseBuilder setCharset(String charset) {
      this.charset = charset;
      return this;
    }

    public Integer getStatus() {
      return status;
    }

    public HttpResponseBuilder setStatus(Integer status) {
      this.status = status;
      return this;
    }

    public String getStatusText() {
      return statusText;
    }

    public HttpResponseBuilder setStatusText(String statusText) {
      this.statusText = statusText;
      return this;
    }

    public URI getUri() {
      return uri;
    }

    public HttpResponseBuilder setUri(URI uri) {
      this.uri = uri;
      return this;
    }

    public Boolean isRedirect() {
      return isRedirect;
    }

    public HttpResponseBuilder setIsRedirect(Boolean isRedirect) {
      this.isRedirect = isRedirect;
      return this;
    }

    public Map<String, String> getResponseHeaders() {
      return responseHeaders;
    }

    public HttpResponseBuilder setResponseHeaders(Map<String, String> responseHeaders) {
      this.responseHeaders = responseHeaders;
      return this;
    }
  }
}
