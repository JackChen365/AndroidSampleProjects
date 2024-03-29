/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.cz.android.simplehttp.header;

import com.cz.android.simplehttp.pool.Util;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * The HTTP status and unparsed header fields of a single HTTP message. Values
 * are represented as uninterpreted strings; use {@link RequestHeaders} and
 * {@link ResponseHeaders} for interpreted headers. This class maintains the
 * order of the header fields within the HTTP message.
 *
 * <p>This class tracks fields line-by-line. A field with multiple comma-
 * separated values on the same line will be treated as a field with a single
 * value by this class. It is the caller's responsibility to detect and split
 * on commas if their field permits multiple values. This simplifies use of
 * single-valued fields whose values routinely contain commas, such as cookies
 * or dates.
 *
 * <p>This class trims whitespace from values. It never returns values with
 * leading or trailing whitespace.
 */
public final class RawHeaders {
  public static final int HTTP_CONTINUE = 100;

  private static final Comparator<String> FIELD_NAME_COMPARATOR = new Comparator<String>() {
    // @FindBugsSuppressWarnings("ES_COMPARING_PARAMETER_STRING_WITH_EQ")
    @Override public int compare(String a, String b) {
      if (a == b) {
        return 0;
      } else if (a == null) {
        return -1;
      } else if (b == null) {
        return 1;
      } else {
        return String.CASE_INSENSITIVE_ORDER.compare(a, b);
      }
    }
  };

  private final List<String> namesAndValues = new ArrayList<String>(20);
  private String requestLine;
  private String statusLine;
  private int httpMinorVersion = 1;
  private int responseCode = -1;
  private String responseMessage;

  public RawHeaders() {
  }

  public RawHeaders(RawHeaders copyFrom) {
    namesAndValues.addAll(copyFrom.namesAndValues);
    requestLine = copyFrom.requestLine;
    statusLine = copyFrom.statusLine;
    httpMinorVersion = copyFrom.httpMinorVersion;
    responseCode = copyFrom.responseCode;
    responseMessage = copyFrom.responseMessage;
  }

  /** Sets the request line (like "GET / HTTP/1.1"). */
  public void setRequestLine(String requestLine) {
    requestLine = requestLine.trim();
    this.requestLine = requestLine;
  }

  /** Sets the response status line (like "HTTP/1.0 200 OK"). */
  public void setStatusLine(String statusLine) throws IOException {
    // H T T P / 1 . 1   2 0 0   T e m p o r a r y   R e d i r e c t
    // 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0
    if (this.responseMessage != null) {
      throw new IllegalStateException("statusLine is already set");
    }
    // We allow empty message without leading white space since some servers
    // do not send the white space when the message is empty.
    boolean hasMessage = statusLine.length() > 13;
    if (!statusLine.startsWith("HTTP/1.")
        || statusLine.length() < 12
        || statusLine.charAt(8) != ' '
        || (hasMessage && statusLine.charAt(12) != ' ')) {
      throw new ProtocolException("Unexpected status line: " + statusLine);
    }
    int httpMinorVersion = statusLine.charAt(7) - '0';
    if (httpMinorVersion < 0 || httpMinorVersion > 9) {
      throw new ProtocolException("Unexpected status line: " + statusLine);
    }
    int responseCode;
    try {
      responseCode = Integer.parseInt(statusLine.substring(9, 12));
    } catch (NumberFormatException e) {
      throw new ProtocolException("Unexpected status line: " + statusLine);
    }
    this.responseMessage = hasMessage ? statusLine.substring(13) : "";
    this.responseCode = responseCode;
    this.statusLine = statusLine;
    this.httpMinorVersion = httpMinorVersion;
  }

  public void computeResponseStatusLineFromSpdyHeaders() throws IOException {
    String status = null;
    String version = null;
    for (int i = 0; i < namesAndValues.size(); i += 2) {
      String name = namesAndValues.get(i);
      if (":status".equals(name)) {
        status = namesAndValues.get(i + 1);
      } else if (":version".equals(name)) {
        version = namesAndValues.get(i + 1);
      }
    }
    if (status == null || version == null) {
      throw new ProtocolException("Expected ':status' and ':version' headers not present");
    }
    setStatusLine(version + " " + status);
  }

  public String getStatusLine() {
    return statusLine;
  }

  /**
   * Returns the status line's HTTP minor version. This returns 0 for HTTP/1.0
   * and 1 for HTTP/1.1. This returns 1 if the HTTP version is unknown.
   */
  public int getHttpMinorVersion() {
    return httpMinorVersion != -1 ? httpMinorVersion : 1;
  }

  /** Returns the HTTP status code or -1 if it is unknown. */
  public int getResponseCode() {
    return responseCode;
  }

  /** Returns the HTTP status message or null if it is unknown. */
  public String getResponseMessage() {
    return responseMessage;
  }

  /**
   * Add an HTTP header line containing a field name, a literal colon, and a
   * value.
   */
  public void addLine(String line) {
    int index = line.indexOf(":");
    if (index == -1) {
      addLenient("", line);
    } else {
      addLenient(line.substring(0, index), line.substring(index + 1));
    }
  }

  /** Add a field with the specified value. */
  public void add(String fieldName, String value) {
    if (fieldName == null) throw new IllegalArgumentException("fieldname == null");
    if (value == null) throw new IllegalArgumentException("value == null");
    if (fieldName.length() == 0 || fieldName.indexOf('\0') != -1 || value.indexOf('\0') != -1) {
      throw new IllegalArgumentException("Unexpected header: " + fieldName + ": " + value);
    }
    addLenient(fieldName, value);
  }

  /**
   * Add a field with the specified value without any validation. Only
   * appropriate for headers from the remote peer.
   */
  private void addLenient(String fieldName, String value) {
    namesAndValues.add(fieldName);
    namesAndValues.add(value.trim());
  }

  public void removeAll(String fieldName) {
    for (int i = 0; i < namesAndValues.size(); i += 2) {
      if (fieldName.equalsIgnoreCase(namesAndValues.get(i))) {
        namesAndValues.remove(i); // field name
        namesAndValues.remove(i); // value
      }
    }
  }

  public void addAll(String fieldName, List<String> headerFields) {
    for (String value : headerFields) {
      add(fieldName, value);
    }
  }

  /**
   * Set a field with the specified value. If the field is not found, it is
   * added. If the field is found, the existing values are replaced.
   */
  public void set(String fieldName, String value) {
    removeAll(fieldName);
    add(fieldName, value);
  }

  /** Returns the number of field values. */
  public int length() {
    return namesAndValues.size() / 2;
  }

  /** Returns the field at {@code position} or null if that is out of range. */
  public String getFieldName(int index) {
    int fieldNameIndex = index * 2;
    if (fieldNameIndex < 0 || fieldNameIndex >= namesAndValues.size()) {
      return null;
    }
    return namesAndValues.get(fieldNameIndex);
  }

  /** Returns the value at {@code index} or null if that is out of range. */
  public String getValue(int index) {
    int valueIndex = index * 2 + 1;
    if (valueIndex < 0 || valueIndex >= namesAndValues.size()) {
      return null;
    }
    return namesAndValues.get(valueIndex);
  }

  /** Returns the last value corresponding to the specified field, or null. */
  public String get(String fieldName) {
    for (int i = namesAndValues.size() - 2; i >= 0; i -= 2) {
      if (fieldName.equalsIgnoreCase(namesAndValues.get(i))) {
        return namesAndValues.get(i + 1);
      }
    }
    return null;
  }

  /** @param fieldNames a case-insensitive set of HTTP header field names. */
  public RawHeaders getAll(Set<String> fieldNames) {
    RawHeaders result = new RawHeaders();
    for (int i = 0; i < namesAndValues.size(); i += 2) {
      String fieldName = namesAndValues.get(i);
      if (fieldNames.contains(fieldName)) {
        result.add(fieldName, namesAndValues.get(i + 1));
      }
    }
    return result;
  }

  /** Returns bytes of a request header for sending on an HTTP transport. */
  public byte[] toBytes() throws UnsupportedEncodingException {
    StringBuilder result = new StringBuilder(256);
    result.append(requestLine).append("\r\n");
    for (int i = 0; i < namesAndValues.size(); i += 2) {
      result.append(namesAndValues.get(i))
          .append(": ")
          .append(namesAndValues.get(i + 1))
          .append("\r\n");
    }
    result.append("\r\n");
    return result.toString().getBytes("ISO-8859-1");
  }

  /** Parses bytes of a response header from an HTTP transport. */
  public static RawHeaders fromBytes(InputStream in) throws IOException {
    RawHeaders headers;
    do {
      headers = new RawHeaders();
      headers.setStatusLine(Util.readAsciiLine(in));
      readHeaders(in, headers);
    } while (headers.getResponseCode() == HTTP_CONTINUE);
    return headers;
  }

  /** Reads headers or trailers into {@code out}. */
  public static void readHeaders(InputStream in, RawHeaders out) throws IOException {
    // parse the result headers until the first blank line
    String line;
    while ((line = Util.readAsciiLine(in)).length() != 0) {
      out.addLine(line);
    }
  }

}
