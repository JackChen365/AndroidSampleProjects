package com.cz.android.simplehttp.request;

import com.cz.android.simplehttp.request.intercept.Interceptor;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.cz.android.simplehttp.request.Util.checkDuration;


/**
 * Factory for {@linkplain Call calls}, which can be used to send HTTP requests and read their
 * responses.
 *
 * <h3>OkHttpClients should be shared</h3>
 *
 * <p>OkHttp performs best when you create a single {@code OkHttpClient} instance and reuse it for
 * all of your HTTP calls. This is because each client holds its own connection pool and thread
 * pools. Reusing connections and threads reduces latency and saves memory. Conversely, creating a
 * client for each request wastes resources on idle pools.
 *
 * <p>Use {@code new OkHttpClient()} to create a shared instance with the default settings:
 * <pre>   {@code
 *
 *   // The singleton HTTP client.
 *   public final OkHttpClient client = new OkHttpClient();
 * }</pre>
 *
 * <p>Or use {@code new OkHttpClient.Builder()} to create a shared instance with custom settings:
 * <pre>   {@code
 *
 *   // The singleton HTTP client.
 *   public final OkHttpClient client = new OkHttpClient.Builder()
 *       .addInterceptor(new HttpLoggingInterceptor())
 *       .cache(new Cache(cacheDir, cacheSize))
 *       .build();
 * }</pre>
 *
 * <h3>Customize your client with newBuilder()</h3>
 *
 * <p>You can customize a shared OkHttpClient instance with {@link #newBuilder()}. This builds a
 * client that shares the same connection pool, thread pools, and configuration. Use the builder
 * methods to configure the derived client for a specific purpose.
 *
 * <p>This example shows a call with a short 500 millisecond timeout: <pre>   {@code
 *
 *   OkHttpClient eagerClient = client.newBuilder()
 *       .readTimeout(500, TimeUnit.MILLISECONDS)
 *       .build();
 *   Response response = eagerClient.newCall(request).execute();
 * }</pre>
 *
 * <h3>Shutdown isn't necessary</h3>
 *
 * <p>The threads and connections that are held will be released automatically if they remain idle.
 * But if you are writing a application that needs to aggressively release unused resources you may
 * do so.
 *
 * <p>Shutdown the dispatcher's executor service with {@link ExecutorService#shutdown shutdown()}.
 * This will also cause future calls to the client to be rejected. <pre>   {@code
 *
 *     client.dispatcher().executorService().shutdown();
 * }</pre>
 *
 *
 * <p>OkHttp also uses daemon threads for HTTP/2 connections. These will exit automatically if they
 * remain idle.
 */
public class RequestClient implements Cloneable, Call.Factory{

  final Dispatcher dispatcher;
  final List<Interceptor> interceptors;
  final List<Interceptor> networkInterceptors;
  final boolean followSslRedirects;
  final boolean followRedirects;
  final boolean retryOnConnectionFailure;
  final int callTimeout;
  final int connectTimeout;
  final int readTimeout;
  final int writeTimeout;

  public RequestClient() {
    this(new Builder());
  }

  RequestClient(Builder builder) {
    this.dispatcher = builder.dispatcher;
    this.interceptors = Util.immutableList(builder.interceptors);
    this.networkInterceptors = Util.immutableList(builder.networkInterceptors);
    this.followSslRedirects = builder.followSslRedirects;
    this.followRedirects = builder.followRedirects;
    this.retryOnConnectionFailure = builder.retryOnConnectionFailure;
    this.callTimeout = builder.callTimeout;
    this.connectTimeout = builder.connectTimeout;
    this.readTimeout = builder.readTimeout;
    this.writeTimeout = builder.writeTimeout;

    if (interceptors.contains(null)) {
      throw new IllegalStateException("Null interceptor: " + interceptors);
    }
    if (networkInterceptors.contains(null)) {
      throw new IllegalStateException("Null network interceptor: " + networkInterceptors);
    }
  }

  /** Default call timeout (in milliseconds). */
  public int callTimeoutMillis() {
    return callTimeout;
  }

  /** Default connect timeout (in milliseconds). */
  public int connectTimeoutMillis() {
    return connectTimeout;
  }

  /** Default read timeout (in milliseconds). */
  public int readTimeoutMillis() {
    return readTimeout;
  }

  /** Default write timeout (in milliseconds). */
  public int writeTimeoutMillis() {
    return writeTimeout;
  }

  public Dispatcher dispatcher() {
    return dispatcher;
  }

  /**
   * Returns an immutable list of interceptors that observe the full span of each call: from before
   * the connection is established (if any) until after the response source is selected (either the
   * origin server, cache, or both).
   */
  public List<Interceptor> interceptors() {
    return interceptors;
  }

  /**
   * Returns an immutable list of interceptors that observe a single network request and response.
   * These interceptors must call {@link Interceptor.Chain#proceed} exactly once: it is an error for
   * a network interceptor to short-circuit or repeat a network request.
   */
  public List<Interceptor> networkInterceptors() {
    return networkInterceptors;
  }

  /**
   * Prepares the {@code request} to be executed at some point in the future.
   */
  @Override public Call newCall(Request request) {
    return RealCall.newRealCall(this, request, false);
  }


  public Builder newBuilder() {
    return new Builder(this);
  }

  public static final class Builder {
    Dispatcher dispatcher;
    final List<Interceptor> interceptors = new ArrayList<>();
    final List<Interceptor> networkInterceptors = new ArrayList<>();
    boolean followSslRedirects;
    boolean followRedirects;
    boolean retryOnConnectionFailure;
    int callTimeout;
    int connectTimeout;
    int readTimeout;
    int writeTimeout;

    public Builder() {
      dispatcher = new Dispatcher();
      followSslRedirects = true;
      followRedirects = true;
      retryOnConnectionFailure = true;
      callTimeout = 0;
      connectTimeout = 10_000;
      readTimeout = 10_000;
      writeTimeout = 10_000;
    }

    Builder(RequestClient okHttpClient) {
      this.dispatcher = okHttpClient.dispatcher;
      this.interceptors.addAll(okHttpClient.interceptors);
      this.networkInterceptors.addAll(okHttpClient.networkInterceptors);
      this.followSslRedirects = okHttpClient.followSslRedirects;
      this.followRedirects = okHttpClient.followRedirects;
      this.retryOnConnectionFailure = okHttpClient.retryOnConnectionFailure;
      this.callTimeout = okHttpClient.callTimeout;
      this.connectTimeout = okHttpClient.connectTimeout;
      this.readTimeout = okHttpClient.readTimeout;
      this.writeTimeout = okHttpClient.writeTimeout;
    }

    /**
     * Sets the default timeout for complete calls. A value of 0 means no timeout, otherwise values
     * must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     *
     * <p>The call timeout spans the entire call: resolving DNS, connecting, writing the request
     * body, server processing, and reading the response body. If the call requires redirects or
     * retries all must complete within one timeout period.
     */
    public Builder callTimeout(long timeout, TimeUnit unit) {
      callTimeout = checkDuration("timeout", timeout, unit);
      return this;
    }

    /**
     * Sets the default timeout for complete calls. A value of 0 means no timeout, otherwise values
     * must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     *
     * <p>The call timeout spans the entire call: resolving DNS, connecting, writing the request
     * body, server processing, and reading the response body. If the call requires redirects or
     * retries all must complete within one timeout period.
     */
    public Builder callTimeout(Duration duration) {
      callTimeout = checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
      return this;
    }

    /**
     * Sets the default connect timeout for new connections. A value of 0 means no timeout,
     * otherwise values must be between 1 and {@link Integer#MAX_VALUE} when converted to
     * milliseconds.
     *
     * <p>The connect timeout is applied when connecting a TCP socket to the target host.
     * The default value is 10 seconds.
     */
    public Builder connectTimeout(long timeout, TimeUnit unit) {
      connectTimeout = checkDuration("timeout", timeout, unit);
      return this;
    }

    /**
     * Sets the default connect timeout for new connections. A value of 0 means no timeout,
     * otherwise values must be between 1 and {@link Integer#MAX_VALUE} when converted to
     * milliseconds.
     *
     * <p>The connect timeout is applied when connecting a TCP socket to the target host.
     * The default value is 10 seconds.
     */
    public Builder connectTimeout(Duration duration) {
      connectTimeout = checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
      return this;
    }

    /**
     * Sets the default read timeout for new connections. A value of 0 means no timeout, otherwise
     * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     *
     */
    public Builder readTimeout(long timeout, TimeUnit unit) {
      readTimeout = checkDuration("timeout", timeout, unit);
      return this;
    }

    /**
     * Sets the default read timeout for new connections. A value of 0 means no timeout, otherwise
     * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     */
    public Builder readTimeout(Duration duration) {
      readTimeout = checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
      return this;
    }

    /**
     * Sets the default write timeout for new connections. A value of 0 means no timeout, otherwise
     * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     *
     * <p>The write timeout is applied for individual write IO operations.
     * The default value is 10 seconds.
     *
     */
    public Builder writeTimeout(long timeout, TimeUnit unit) {
      writeTimeout = checkDuration("timeout", timeout, unit);
      return this;
    }

    /**
     * Sets the default write timeout for new connections. A value of 0 means no timeout, otherwise
     * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     *
     * <p>The write timeout is applied for individual write IO operations.
     * The default value is 10 seconds.
     */
    public Builder writeTimeout(Duration duration) {
      writeTimeout = checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
      return this;
    }


    /**
     * Configure this client to retry or not when a connectivity problem is encountered. By default,
     * this client silently recovers from the following problems:
     *
     * <ul>
     *   <li><strong>Unreachable IP addresses.</strong> If the URL's host has multiple IP addresses,
     *       failure to reach any individual IP address doesn't fail the overall request. This can
     *       increase availability of multi-homed services.
     *   <li><strong>Stale pooled connections.</strong> The @link ConnectionPool reuses sockets
     *       to decrease request latency, but these connections will occasionally time out.
     *   <li><strong>Unreachable proxy servers.</strong> A {@link ProxySelector} can be used to
     *       attempt multiple proxy servers in sequence, eventually falling back to a direct
     *       connection.
     * </ul>
     *
     * Set this to false to avoid retrying requests when doing so is destructive. In this case the
     * calling application should do its own recovery of connectivity failures.
     */
    public Builder retryOnConnectionFailure(boolean retryOnConnectionFailure) {
      this.retryOnConnectionFailure = retryOnConnectionFailure;
      return this;
    }

    /**
     * Sets the dispatcher used to set policy and execute asynchronous requests. Must not be null.
     */
    public Builder dispatcher(Dispatcher dispatcher) {
      if (dispatcher == null) throw new IllegalArgumentException("dispatcher == null");
      this.dispatcher = dispatcher;
      return this;
    }

    /**
     * Returns a modifiable list of interceptors that observe the full span of each call: from
     * before the connection is established (if any) until after the response source is selected
     * (either the origin server, cache, or both).
     */
    public List<Interceptor> interceptors() {
      return interceptors;
    }

    public Builder addInterceptor(Interceptor interceptor) {
      if (interceptor == null) throw new IllegalArgumentException("interceptor == null");
      interceptors.add(interceptor);
      return this;
    }

    /**
     * Returns a modifiable list of interceptors that observe a single network request and response.
     * These interceptors must call {@link Interceptor.Chain#proceed} exactly once: it is an error
     * for a network interceptor to short-circuit or repeat a network request.
     */
    public List<Interceptor> networkInterceptors() {
      return networkInterceptors;
    }

    public Builder addNetworkInterceptor(Interceptor interceptor) {
      if (interceptor == null) throw new IllegalArgumentException("interceptor == null");
      networkInterceptors.add(interceptor);
      return this;
    }

    public RequestClient build() {
      return new RequestClient(this);
    }
  }
}
