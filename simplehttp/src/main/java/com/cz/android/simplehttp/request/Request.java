package com.cz.android.simplehttp.request;

import com.sun.istack.internal.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * A request.
 */
public final class Request {
  final String url;
  final Map<Class<?>, Object> tags;

  Request(Builder builder) {
    this.url = builder.url;
    this.tags = Util.immutableMap(builder.tags);
  }

  public String url() {
    return url;
  }

  /**
   * Returns the tag attached with {@code Object.class} as a key, or null if no tag is attached with
   * that key.
   *
   * <p>Prior to OkHttp 3.11, this method never returned null if no tag was attached. Instead it
   * returned either this request, or the request upon which this request was derived with {@link
   * #newBuilder()}.
   */
  public @Nullable Object tag() {
    return tag(Object.class);
  }

  /**
   * Returns the tag attached with {@code type} as a key, or null if no tag is attached with that
   * key.
   */
  public @Nullable <T> T tag(Class<? extends T> type) {
    return type.cast(tags.get(type));
  }

  public Builder newBuilder() {
    return new Builder(this);
  }

  @Override public String toString() {
    return "Request{method="
        + ", url="
        + url
        + ", tags="
        + tags
        + '}';
  }

  public static class Builder {
    String url;
    /** A mutable map of tags, or an immutable empty map if we don't have any. */
    Map<Class<?>, Object> tags = Collections.emptyMap();

    public Builder() {
    }

    Builder(Request request) {
      this.url = request.url;
      this.tags = request.tags.isEmpty()
          ? Collections.<Class<?>, Object>emptyMap()
          : new LinkedHashMap<>(request.tags);
    }

    /**
     * Sets the URL target of this request.
     *
     * @throws IllegalArgumentException if {@code url} is not a valid HTTP or HTTPS URL. Avoid this
     * exception by calling {@link HttpUrl#parse}; it returns null for invalid URLs.
     */
    public Builder url(String url) {
      if (url == null) throw new NullPointerException("url == null");

      // Silently replace web socket URLs with HTTP URLs.
      if (url.regionMatches(true, 0, "ws:", 0, 3)) {
        url = "http:" + url.substring(3);
      } else if (url.regionMatches(true, 0, "wss:", 0, 4)) {
        url = "https:" + url.substring(4);
      }

      return this;
    }

    /** Attaches {@code tag} to the request using {@code Object.class} as a key. */
    public Builder tag(@Nullable Object tag) {
      return tag(Object.class, tag);
    }

    /**
     * Attaches {@code tag} to the request using {@code type} as a key. Tags can be read from a
     * request using {@link Request#tag}. Use null to remove any existing tag assigned for {@code
     * type}.
     *
     * <p>Use this API to attach timing, debugging, or other application data to a request so that
     * you may read it in interceptors, event listeners, or callbacks.
     */
    public <T> Builder tag(Class<? super T> type, @Nullable T tag) {
      if (type == null) throw new NullPointerException("type == null");

      if (tag == null) {
        tags.remove(type);
      } else {
        if (tags.isEmpty()) tags = new LinkedHashMap<>();
        tags.put(type, type.cast(tag));
      }

      return this;
    }

    public Request build() {
      if (url == null) throw new IllegalStateException("url == null");
      return new Request(this);
    }
  }
}
