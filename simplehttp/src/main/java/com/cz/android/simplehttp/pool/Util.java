/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cz.android.simplehttp.pool;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

/** Junk drawer of utility methods. */
public final class Util {

  private Util() {
  }

  /**
   * Closes {@code closeable}, ignoring any checked exceptions. Does nothing
   * if {@code closeable} is null.
   */
  public static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (RuntimeException rethrown) {
        throw rethrown;
      } catch (Exception ignored) {
      }
    }
  }

  public static ThreadFactory daemonThreadFactory(final String name) {
    return new ThreadFactory() {
      @Override public Thread newThread(Runnable runnable) {
        Thread result = new Thread(runnable, name);
        result.setDaemon(true);
        return result;
      }
    };
  }

  /**
   * Returns the ASCII characters up to but not including the next "\r\n", or
   * "\n".
   *
   * @throws EOFException if the stream is exhausted before the next newline
   * character.
   */
  public static String readAsciiLine(InputStream in) throws IOException {
    // TODO: support UTF-8 here instead
    StringBuilder result = new StringBuilder(80);
    while (true) {
      int c = in.read();
      if (c == -1) {
        throw new EOFException();
      } else if (c == '\n') {
        break;
      }

      result.append((char) c);
    }
    int length = result.length();
    if (length > 0 && result.charAt(length - 1) == '\r') {
      result.setLength(length - 1);
    }
    return result.toString();
  }

  /** Returns true if two possibly-null objects are equal. */
  public static boolean equal(Object a, Object b) {
    return Objects.equals(a, b);
  }
}
