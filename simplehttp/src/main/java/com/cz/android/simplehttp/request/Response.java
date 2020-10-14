package com.cz.android.simplehttp.request;

import com.sun.istack.internal.Nullable;

import java.io.Closeable;

/**
 * An request response. Instances of this class are not immutable: the response body is a one-shot
 * value that may be consumed only once and then closed. All other properties are immutable.
 *
 */
public final class Response {
  final Request request;
  final int code;
  final String message;
  final @Nullable Response networkResponse;
  final @Nullable Response cacheResponse;
  final @Nullable Response priorResponse;
  final long sentRequestAtMillis;
  final long receivedResponseAtMillis;

  Response(Builder builder) {
    this.request = builder.request;
    this.code = builder.code;
    this.message = builder.message;
    this.networkResponse = builder.networkResponse;
    this.cacheResponse = builder.cacheResponse;
    this.priorResponse = builder.priorResponse;
    this.sentRequestAtMillis = builder.sentRequestAtMillis;
    this.receivedResponseAtMillis = builder.receivedResponseAtMillis;
  }

  /**
   * The wire-level request that initiated this HTTP response. This is not necessarily the same
   * request issued by the application:
   *
   * <ul>
   *     <li>It may be transformed by the HTTP client. For example, the client may copy headers like
   *         {@code Content-Length} from the request body.
   *     <li>It may be the request generated in response to an HTTP redirect or authentication
   *         challenge. In this case the request URL may be different than the initial request URL.
   * </ul>
   */
  public Request request() {
    return request;
  }

  /** Returns the HTTP status code. */
  public int code() {
    return code;
  }

  /**
   * Returns true if the code is in [200..300), which means the request was successfully received,
   * understood, and accepted.
   */
  public boolean isSuccessful() {
    return code >= 200 && code < 300;
  }

  /** Returns the HTTP status message. */
  public String message() {
    return message;
  }

  public Builder newBuilder() {
    return new Builder(this);
  }

  /**
   * Returns the raw response received from the network. Will be null if this response didn't use
   * the network, such as when the response is fully cached. The body of the returned response
   * should not be read.
   */
  public @Nullable Response networkResponse() {
    return networkResponse;
  }

  /**
   * Returns the raw response received from the cache. Will be null if this response didn't use the
   * cache. For conditional get requests the cache response and network response may both be
   * non-null. The body of the returned response should not be read.
   */
  public @Nullable Response cacheResponse() {
    return cacheResponse;
  }

  /**
   * Returns the response for the HTTP redirect or authorization challenge that triggered this
   * response, or null if this response wasn't triggered by an automatic retry. The body of the
   * returned response should not be read because it has already been consumed by the redirecting
   * client.
   */
  public @Nullable Response priorResponse() {
    return priorResponse;
  }

  /**
   * Returns a {@linkplain System#currentTimeMillis() timestamp} taken immediately before OkHttp
   * transmitted the initiating request over the network. If this response is being served from the
   * cache then this is the timestamp of the original request.
   */
  public long sentRequestAtMillis() {
    return sentRequestAtMillis;
  }

  /**
   * Returns a {@linkplain System#currentTimeMillis() timestamp} taken immediately after OkHttp
   * received this response's headers from the network. If this response is being served from the
   * cache then this is the timestamp of the original response.
   */
  public long receivedResponseAtMillis() {
    return receivedResponseAtMillis;
  }

  @Override public String toString() {
    return "Response{protocol="
        + ", code="
        + code
        + ", message="
        + message
        + ", url="
        + request.url()
        + '}';
  }

  public static class Builder {
    @Nullable Request request;
    int code = -1;
    String message;
    @Nullable Response networkResponse;
    @Nullable Response cacheResponse;
    @Nullable Response priorResponse;
    long sentRequestAtMillis;
    long receivedResponseAtMillis;

    public Builder() {
    }

    Builder(Response response) {
      this.request = response.request;
      this.code = response.code;
      this.message = response.message;
      this.networkResponse = response.networkResponse;
      this.cacheResponse = response.cacheResponse;
      this.priorResponse = response.priorResponse;
      this.sentRequestAtMillis = response.sentRequestAtMillis;
      this.receivedResponseAtMillis = response.receivedResponseAtMillis;
    }

    public Builder request(Request request) {
      this.request = request;
      return this;
    }

    public Builder code(int code) {
      this.code = code;
      return this;
    }

    public Builder message(String message) {
      this.message = message;
      return this;
    }

    public Builder networkResponse(@Nullable Response networkResponse) {
      if (networkResponse != null) checkSupportResponse("networkResponse", networkResponse);
      this.networkResponse = networkResponse;
      return this;
    }

    public Builder cacheResponse(@Nullable Response cacheResponse) {
      if (cacheResponse != null) checkSupportResponse("cacheResponse", cacheResponse);
      this.cacheResponse = cacheResponse;
      return this;
    }

    private void checkSupportResponse(String name, Response response) {
      if (response.networkResponse != null) {
        throw new IllegalArgumentException(name + ".networkResponse != null");
      } else if (response.cacheResponse != null) {
        throw new IllegalArgumentException(name + ".cacheResponse != null");
      } else if (response.priorResponse != null) {
        throw new IllegalArgumentException(name + ".priorResponse != null");
      }
    }

    public Builder priorResponse(@Nullable Response priorResponse) {
      this.priorResponse = priorResponse;
      return this;
    }

    public Builder sentRequestAtMillis(long sentRequestAtMillis) {
      this.sentRequestAtMillis = sentRequestAtMillis;
      return this;
    }

    public Builder receivedResponseAtMillis(long receivedResponseAtMillis) {
      this.receivedResponseAtMillis = receivedResponseAtMillis;
      return this;
    }

    public Response build() {
      if (request == null) throw new IllegalStateException("request == null");
      if (code < 0) throw new IllegalStateException("code < 0: " + code);
      if (message == null) throw new IllegalStateException("message == null");
      return new Response(this);
    }
  }
}
