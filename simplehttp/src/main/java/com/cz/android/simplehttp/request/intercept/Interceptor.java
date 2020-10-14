package com.cz.android.simplehttp.request.intercept;

import com.cz.android.simplehttp.request.Call;
import com.cz.android.simplehttp.request.Request;
import com.cz.android.simplehttp.request.Response;
import com.sun.istack.internal.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * Observes, modifies, and potentially short-circuits requests going out and the corresponding
 * responses coming back in. Typically interceptors add, remove, or transform headers on the request
 * or response.
 */
public interface Interceptor {
  Response intercept(Chain chain) throws IOException;

  interface Chain {
    Request request();

    Response proceed(Request request) throws IOException;

    Call call();

    int connectTimeoutMillis();

    Chain withConnectTimeout(int timeout, TimeUnit unit);

    int readTimeoutMillis();

    Chain withReadTimeout(int timeout, TimeUnit unit);

    int writeTimeoutMillis();

    Chain withWriteTimeout(int timeout, TimeUnit unit);
  }
}
