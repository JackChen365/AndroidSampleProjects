package com.cz.android.simplehttp.request;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


/** Junk drawer of utility methods. */
public final class Util {
  public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
  public static final String[] EMPTY_STRING_ARRAY = new String[0];

  public static final Charset UTF_8 = Charset.forName("UTF-8");
  public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
  private static final Charset UTF_16_BE = Charset.forName("UTF-16BE");
  private static final Charset UTF_16_LE = Charset.forName("UTF-16LE");
  private static final Charset UTF_32_BE = Charset.forName("UTF-32BE");
  private static final Charset UTF_32_LE = Charset.forName("UTF-32LE");

  /** GMT and UTC are equivalent for our purposes. */
  public static final TimeZone UTC = TimeZone.getTimeZone("GMT");

  public static final Comparator<String> NATURAL_ORDER = new Comparator<String>() {
    @Override public int compare(String a, String b) {
      return a.compareTo(b);
    }
  };

  private static final Method addSuppressedExceptionMethod;

  static {
    Method m;
    try {
      m = Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class);
    } catch (Exception e) {
      m = null;
    }
    addSuppressedExceptionMethod = m;
  }

  public static void addSuppressedIfPossible(Throwable e, Throwable suppressed) {
    if (addSuppressedExceptionMethod != null) {
      try {
        addSuppressedExceptionMethod.invoke(e, suppressed);
      } catch (InvocationTargetException | IllegalAccessException ignored) {
      }
    }
  }

  /**
   * Quick and dirty pattern to differentiate IP addresses from hostnames. This is an approximation
   * of Android's private InetAddress#isNumeric API.
   *
   * <p>This matches IPv6 addresses as a hex string containing at least one colon, and possibly
   * including dots after the first colon. It matches IPv4 addresses as strings containing only
   * decimal digits and dots. This pattern matches strings like "a:.23" and "54" that are neither IP
   * addresses nor hostnames; they will be verified as IP addresses (which is a more strict
   * verification).
   */
  private static final Pattern VERIFY_AS_IP_ADDRESS = Pattern.compile(
      "([0-9a-fA-F]*:[0-9a-fA-F:.]*)|([\\d.]+)");

  private Util() {
  }

  public static void checkOffsetAndCount(long arrayLength, long offset, long count) {
    if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
      throw new ArrayIndexOutOfBoundsException();
    }
  }

  /** Returns true if two possibly-null objects are equal. */
  public static boolean equal(Object a, Object b) {
    return a == b || (a != null && a.equals(b));
  }

  /**
   * Closes {@code closeable}, ignoring any checked exceptions. Does nothing if {@code closeable} is
   * null.
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

  /**
   * Closes {@code socket}, ignoring any checked exceptions. Does nothing if {@code socket} is
   * null.
   */
  public static void closeQuietly(Socket socket) {
    if (socket != null) {
      try {
        socket.close();
      } catch (RuntimeException rethrown) {
        throw rethrown;
      } catch (Exception ignored) {
      }
    }
  }

  /**
   * Closes {@code serverSocket}, ignoring any checked exceptions. Does nothing if {@code
   * serverSocket} is null.
   */
  public static void closeQuietly(ServerSocket serverSocket) {
    if (serverSocket != null) {
      try {
        serverSocket.close();
      } catch (RuntimeException rethrown) {
        throw rethrown;
      } catch (Exception ignored) {
      }
    }
  }

  public static int checkDuration(String name, long duration, TimeUnit unit) {
    if (duration < 0) throw new IllegalArgumentException(name + " < 0");
    if (unit == null) throw new NullPointerException("unit == null");
    long millis = unit.toMillis(duration);
    if (millis > Integer.MAX_VALUE) throw new IllegalArgumentException(name + " too large.");
    if (millis == 0 && duration > 0) throw new IllegalArgumentException(name + " too small.");
    return (int) millis;
  }

  /** Returns an immutable copy of {@code list}. */
  public static <T> List<T> immutableList(List<T> list) {
    return Collections.unmodifiableList(new ArrayList<>(list));
  }

  /** Returns an immutable copy of {@code map}. */
  public static <K, V> Map<K, V> immutableMap(Map<K, V> map) {
    return map.isEmpty()
        ? Collections.emptyMap()
        : Collections.unmodifiableMap(new LinkedHashMap<>(map));
  }

  /** Returns an immutable list containing {@code elements}. */
  public static <T> List<T> immutableList(T... elements) {
    return Collections.unmodifiableList(Arrays.asList(elements.clone()));
  }

  public static ThreadFactory threadFactory(final String name, final boolean daemon) {
    return new ThreadFactory() {
      @Override public Thread newThread(Runnable runnable) {
        Thread result = new Thread(runnable, name);
        result.setDaemon(daemon);
        return result;
      }
    };
  }

  /**
   * Returns an array containing only elements found in {@code first} and also in {@code
   * second}. The returned elements are in the same order as in {@code first}.
   */
  @SuppressWarnings("unchecked")
  public static String[] intersect(
      Comparator<? super String> comparator, String[] first, String[] second) {
    List<String> result = new ArrayList<>();
    for (String a : first) {
      for (String b : second) {
        if (comparator.compare(a, b) == 0) {
          result.add(a);
          break;
        }
      }
    }
    return result.toArray(new String[result.size()]);
  }

  /**
   * Returns true if there is an element in {@code first} that is also in {@code second}. This
   * method terminates if any intersection is found. The sizes of both arguments are assumed to be
   * so small, and the likelihood of an intersection so great, that it is not worth the CPU cost of
   * sorting or the memory cost of hashing.
   */
  public static boolean nonEmptyIntersection(
      Comparator<String> comparator, String[] first, String[] second) {
    if (first == null || second == null || first.length == 0 || second.length == 0) {
      return false;
    }
    for (String a : first) {
      for (String b : second) {
        if (comparator.compare(a, b) == 0) {
          return true;
        }
      }
    }
    return false;
  }

  public static int indexOf(Comparator<String> comparator, String[] array, String value) {
    for (int i = 0, size = array.length; i < size; i++) {
      if (comparator.compare(array[i], value) == 0) return i;
    }
    return -1;
  }

  public static String[] concat(String[] array, String value) {
    String[] result = new String[array.length + 1];
    System.arraycopy(array, 0, result, 0, array.length);
    result[result.length - 1] = value;
    return result;
  }

  /**
   * Increments {@code pos} until {@code input[pos]} is not ASCII whitespace. Stops at {@code
   * limit}.
   */
  public static int skipLeadingAsciiWhitespace(String input, int pos, int limit) {
    for (int i = pos; i < limit; i++) {
      switch (input.charAt(i)) {
        case '\t':
        case '\n':
        case '\f':
        case '\r':
        case ' ':
          continue;
        default:
          return i;
      }
    }
    return limit;
  }

  /**
   * Decrements {@code limit} until {@code input[limit - 1]} is not ASCII whitespace. Stops at
   * {@code pos}.
   */
  public static int skipTrailingAsciiWhitespace(String input, int pos, int limit) {
    for (int i = limit - 1; i >= pos; i--) {
      switch (input.charAt(i)) {
        case '\t':
        case '\n':
        case '\f':
        case '\r':
        case ' ':
          continue;
        default:
          return i + 1;
      }
    }
    return pos;
  }

  /** Equivalent to {@code string.substring(pos, limit).trim()}. */
  public static String trimSubstring(String string, int pos, int limit) {
    int start = skipLeadingAsciiWhitespace(string, pos, limit);
    int end = skipTrailingAsciiWhitespace(string, start, limit);
    return string.substring(start, end);
  }

  /**
   * Returns the index of the first character in {@code input} that contains a character in {@code
   * delimiters}. Returns limit if there is no such character.
   */
  public static int delimiterOffset(String input, int pos, int limit, String delimiters) {
    for (int i = pos; i < limit; i++) {
      if (delimiters.indexOf(input.charAt(i)) != -1) return i;
    }
    return limit;
  }

  /**
   * Returns the index of the first character in {@code input} that is {@code delimiter}. Returns
   * limit if there is no such character.
   */
  public static int delimiterOffset(String input, int pos, int limit, char delimiter) {
    for (int i = pos; i < limit; i++) {
      if (input.charAt(i) == delimiter) return i;
    }
    return limit;
  }

  /**
   * Returns the index of the first character in {@code input} that is either a control character
   * (like {@code \u0000 or \n}) or a non-ASCII character. Returns -1 if {@code input} has no such
   * characters.
   */
  public static int indexOfControlOrNonAscii(String input) {
    for (int i = 0, length = input.length(); i < length; i++) {
      char c = input.charAt(i);
      if (c <= '\u001f' || c >= '\u007f') {
        return i;
      }
    }
    return -1;
  }

  /** Returns true if {@code host} is not a host name and might be an IP address. */
  public static boolean verifyAsIpAddress(String host) {
    return VERIFY_AS_IP_ADDRESS.matcher(host).matches();
  }

  /** Returns a {@link Locale#US} formatted {@link String}. */
  public static String format(String format, Object... args) {
    return String.format(Locale.US, format, args);
  }

  public static AssertionError assertionError(String message, Exception e) {
    AssertionError assertionError = new AssertionError(message);
    try {
      assertionError.initCause(e);
    } catch (IllegalStateException ise) {
      // ignored, shouldn't happen
    }
    return assertionError;
  }
}
