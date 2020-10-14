package com.cz.android.simplehttp.request.intercept;

import com.cz.android.simplehttp.request.Call;
import com.cz.android.simplehttp.request.Request;
import com.cz.android.simplehttp.request.RequestClient;
import com.cz.android.simplehttp.request.Response;

import java.io.IOException;


/**
 * This interceptor recovers from failures and follows redirects as necessary. It may throw an
 * {@link IOException} if the call was canceled.
 */
public final class RetryAndFollowUpInterceptor implements Interceptor {
  /**
   * How many redirects and auth challenges should we attempt? Chrome follows 21 redirects; Firefox,
   * curl, and wget follow 20; Safari follows 16; and HTTP/1.0 recommends 5.
   */
  private static final int MAX_FOLLOW_UPS = 20;

  private final RequestClient client;
  private Object callStackTrace;
  private volatile boolean canceled;

  public RetryAndFollowUpInterceptor(RequestClient client) {
    this.client = client;
  }

  /**
   * Immediately closes the socket connection if it's currently held. Use this to interrupt an
   * in-flight request from any thread. It's the caller's responsibility to close the request body
   * and response body streams; otherwise resources may be leaked.
   *
   * <p>This method is safe to be called concurrently, but provides limited guarantees. If a
   * transport layer connection has been established (such as a HTTP/2 stream) that is terminated.
   * Otherwise if a socket connection is being established, that is terminated.
   */
  public void cancel() {
    canceled = true;
  }

  public boolean isCanceled() {
    return canceled;
  }

  public void setCallStackTrace(Object callStackTrace) {
    this.callStackTrace = callStackTrace;
  }

  @Override public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    RealInterceptorChain realChain = (RealInterceptorChain) chain;
    Call call = realChain.call();

    int followUpCount = 0;
    Response priorResponse = null;
    while (true) {
      if (canceled) {
        throw new IOException("Canceled");
      }

      Response response;
      boolean releaseConnection = true;
      try {
        response = realChain.proceed(request);
        releaseConnection = false;
      } catch (IOException e) {
        // An attempt to communicate with a server failed. The request may have been sent.
        continue;
      } finally {
      }

      // Attach the prior response if it exists. Such responses never have a body.
      if (priorResponse != null) {
        response = response.newBuilder()
            .priorResponse(priorResponse.newBuilder()
                    .build())
            .build();
      }

      Request followUp;
      try {
        followUp = followUpRequest(response);
      } catch (IOException e) {
        throw e;
      }

      request = followUp;
      priorResponse = response;
    }
  }

  /**
   * Figures out the HTTP request to make in response to receiving {@code userResponse}. This will
   * either add authentication headers, follow redirects or handle a client request timeout. If a
   * follow-up is either unnecessary or not applicable, this returns null.
   */
  private Request followUpRequest(Response userResponse) throws IOException {
    if (userResponse == null) throw new IllegalStateException();
    int responseCode = userResponse.code();

    return null;
  }

}
