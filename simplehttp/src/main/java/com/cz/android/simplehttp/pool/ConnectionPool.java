package com.cz.android.simplehttp.pool;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ConnectionPool {
  private static final int MAX_CONNECTIONS_TO_CLEANUP = 2;
  private static final long DEFAULT_KEEP_ALIVE_DURATION_MS = 5 * 60 * 1000; // 5 min

  private static final ConnectionPool systemDefault;

  static {
    String keepAlive = System.getProperty("http.keepAlive");
    String keepAliveDuration = System.getProperty("http.keepAliveDuration");
    String maxIdleConnections = System.getProperty("http.maxConnections");
    long keepAliveDurationMs = keepAliveDuration != null ? Long.parseLong(keepAliveDuration)
        : DEFAULT_KEEP_ALIVE_DURATION_MS;
    if (keepAlive != null && !Boolean.parseBoolean(keepAlive)) {
      systemDefault = new ConnectionPool(0, keepAliveDurationMs);
    } else if (maxIdleConnections != null) {
      systemDefault = new ConnectionPool(Integer.parseInt(maxIdleConnections), keepAliveDurationMs);
    } else {
      systemDefault = new ConnectionPool(5, keepAliveDurationMs);
    }
  }

  /** The maximum number of idle connections for each address. */
  private final int maxIdleConnections;
  private final long keepAliveDurationNs;

  private final LinkedList<Connection> connections = new LinkedList<Connection>();

  /** We use a single background thread to cleanup expired connections. */
  private final ExecutorService executorService = new ThreadPoolExecutor(0, 1,
      60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
      Util.daemonThreadFactory("OkHttp ConnectionPool"));
  private final Callable<Void> connectionsCleanupCallable = new Callable<Void>() {
    @Override public Void call() {
      List<Connection> expiredConnections = new ArrayList<Connection>(MAX_CONNECTIONS_TO_CLEANUP);
      int idleConnectionCount = 0;
      synchronized (ConnectionPool.this) {
        for (ListIterator<Connection> i = connections.listIterator(connections.size());
            i.hasPrevious(); ) {
          Connection connection = i.previous();
          if (!connection.isAlive() || connection.isExpired(keepAliveDurationNs)) {
            i.remove();
            expiredConnections.add(connection);
            if (expiredConnections.size() == MAX_CONNECTIONS_TO_CLEANUP)
              break;
          } else {
            idleConnectionCount++;
          }
        }

        for (ListIterator<Connection> i = connections.listIterator(connections.size());
            i.hasPrevious() && idleConnectionCount > maxIdleConnections; ) {
          Connection connection = i.previous();
          expiredConnections.add(connection);
          i.remove();
          --idleConnectionCount;
        }
      }
      for (Connection expiredConnection : expiredConnections) {
        Util.closeQuietly(expiredConnection);
      }
      return null;
    }
  };

  public ConnectionPool(int maxIdleConnections, long keepAliveDurationMs) {
    this.maxIdleConnections = maxIdleConnections;
    this.keepAliveDurationNs = keepAliveDurationMs * 1000 * 1000;
  }

  public static ConnectionPool getDefault() {
    return systemDefault;
  }

  /** Returns total number of connections in the ok. */
  public synchronized int getConnectionCount() {
    return connections.size();
  }

  /** Returns a recycled connection to {@code address}, or null if no such connection exists. */
  public synchronized Connection get(String host,int port) {
    Connection foundConnection = null;
    for (ListIterator<Connection> i = connections.listIterator(connections.size());
        i.hasPrevious(); ) {
      Connection connection = i.previous();
      String connectionHost = connection.getHost();
      int connectionPort = connection.getPort();
      if (!connectionHost.equals(host) || connectionPort!=port || !connection.isAlive()
          || System.nanoTime() - connection.getIdleStartTimeNs() >= keepAliveDurationNs) {
        continue;
      }
      i.remove();
      foundConnection = connection;
      break;
    }
    if (foundConnection != null) {
      connections.addFirst(foundConnection); // Add it back after iteration.
    }
    executorService.submit(connectionsCleanupCallable);
    return foundConnection;
  }

  /**
   * Gives {@code connection} to the ok. The ok may store the connection,
   * or close it, as its policy describes.
   *
   * <p>It is an error to use {@code connection} after calling this method.
   */
  public void recycle(Connection connection) {
    executorService.submit(connectionsCleanupCallable);

    if (!connection.isAlive()) {
      Util.closeQuietly(connection);
      return;
    }
    synchronized (this) {
      connections.addFirst(connection);
      connection.resetIdleStartTime();
    }
  }

  /** Close and remove all connections in the ok. */
  public void evictAll() {
    List<Connection> connections;
    synchronized (this) {
      connections = new ArrayList<>(this.connections);
      this.connections.clear();
    }

    for (Connection connection : connections) {
      Util.closeQuietly(connection);
    }
  }
}
