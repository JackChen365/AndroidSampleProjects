package com.cz.android.simplehttp.request.intercept;

import com.cz.android.simplehttp.request.Call;
import com.cz.android.simplehttp.request.Request;
import com.cz.android.simplehttp.request.Response;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.cz.android.simplehttp.request.Util.checkDuration;


/**
 * A concrete interceptor chain that carries the entire interceptor chain: all application
 * interceptors, the OkHttp core, all network interceptors, and finally the network caller.
 */
public final class RealInterceptorChain implements Interceptor.Chain {
  private final List<Interceptor> interceptors;
  private final int index;
  private final Request request;
  private final Call call;
  private final int connectTimeout;
  private final int readTimeout;
  private final int writeTimeout;
  private int calls;

  public RealInterceptorChain(List<Interceptor> interceptors,
                              int index, Request request, Call call,
                              int connectTimeout, int readTimeout, int writeTimeout) {
    this.interceptors = interceptors;
    this.index = index;
    this.request = request;
    this.call = call;
    this.connectTimeout = connectTimeout;
    this.readTimeout = readTimeout;
    this.writeTimeout = writeTimeout;
  }

  @Override public int connectTimeoutMillis() {
    return connectTimeout;
  }

  @Override public Interceptor.Chain withConnectTimeout(int timeout, TimeUnit unit) {
    int millis = checkDuration("timeout", timeout, unit);
    return new RealInterceptorChain(interceptors, index,
        request, call, millis, readTimeout, writeTimeout);
  }

  @Override public int readTimeoutMillis() {
    return readTimeout;
  }

  @Override public Interceptor.Chain withReadTimeout(int timeout, TimeUnit unit) {
    int millis = checkDuration("timeout", timeout, unit);
    return new RealInterceptorChain(interceptors,  index,
        request, call, connectTimeout, millis, writeTimeout);
  }

  @Override public int writeTimeoutMillis() {
    return writeTimeout;
  }

  @Override public Interceptor.Chain withWriteTimeout(int timeout, TimeUnit unit) {
    int millis = checkDuration("timeout", timeout, unit);
    return new RealInterceptorChain(interceptors, index,
        request, call, connectTimeout, readTimeout, millis);
  }

  @Override public Call call() {
    return call;
  }

  @Override public Request request() {
    return request;
  }

  @Override public Response proceed(Request request) throws IOException {
    return proceedInternal(request);
  }

  public Response proceedInternal(Request request) throws IOException {
    if (index >= interceptors.size()) throw new AssertionError();

    calls++;

    // Call the next interceptor in the chain.
    RealInterceptorChain next = new RealInterceptorChain(interceptors, index + 1, request, call, connectTimeout, readTimeout,
        writeTimeout);
    Interceptor interceptor = interceptors.get(index);
    Response response = interceptor.intercept(next);

    // Confirm that the next interceptor made its required call to chain.proceed().
    if (index + 1 < interceptors.size() && next.calls != 1) {
      throw new IllegalStateException("network interceptor " + interceptor
          + " must call proceed() exactly once");
    }

    // Confirm that the intercepted response isn't null.
    if (response == null) {
      throw new NullPointerException("interceptor " + interceptor + " returned null");
    }
    return response;
  }
}
