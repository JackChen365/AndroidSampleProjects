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
package com.cz.android.simplehttp.pool;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

public final class Connection implements Closeable {
  private Socket socket;
  private String host;
  private int port;
  private InputStream in;
  private OutputStream out;
  private boolean connected = false;
  private long idleStartTimeNs;

  public void connect(String host,int port, int connectTimeout, int readTimeout)
      throws IOException {
    if (connected) {
      throw new IllegalStateException("already connected");
    }
    this.host=host;
    this.port=port;
    connected = true;
    socket = new Socket();

    InetSocketAddress httpSocketAddress = new InetSocketAddress(host,port);
    this.socket.connect(httpSocketAddress, connectTimeout);
    this.socket.setSoTimeout(readTimeout);
    in = this.socket.getInputStream();
    out = this.socket.getOutputStream();

    // Smaller than 1500 to leave room for headers on interfaces like PPPoE.
    int mtu = 1400;
    in = new BufferedInputStream(in, mtu);
    out = new BufferedOutputStream(out, mtu);
  }


  /** Returns true if {@link #connect} has been attempted on this connection. */
  public boolean isConnected() {
    return connected;
  }

  @Override public void close() throws IOException {
    socket.close();
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  /**
   * Returns the socket that this connection uses, or null if the connection
   * is not currently connected.
   */
  public Socket getSocket() {
    return socket;
  }

  /**
   * Returns true if this connection is alive.
   **/
  public boolean isAlive() {
    return !socket.isClosed() && !socket.isInputShutdown() && !socket.isOutputShutdown();
  }

  public void resetIdleStartTime() {
    this.idleStartTimeNs = System.nanoTime();
  }

  /**
   * Returns true if this connection has been idle for longer than
   * {@code keepAliveDurationNs}.
   */
  public boolean isExpired(long keepAliveDurationNs) {
    return System.nanoTime() - getIdleStartTimeNs() > keepAliveDurationNs;
  }

  /**
   * Returns the time in ns when this connection became idle. Undefined if
   * this connection is not idle.
   */
  public long getIdleStartTimeNs() {
    return idleStartTimeNs;
  }

}
