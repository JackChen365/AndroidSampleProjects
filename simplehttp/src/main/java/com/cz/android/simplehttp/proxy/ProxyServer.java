package com.cz.android.simplehttp.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {

    /**
     * Default server port number
     */
    private static final int HTTP_PORT = 8099;

    /**
     * Server socket
     */
    private ServerSocket serverSocket;

    /**
     * Manager classes
     */
    private BlockManager blockManager;
    private CacheManager cacheManager;

    /**
     * Constructor
     *
     * @param port server port number
     */
    public ProxyServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            blockManager = new BlockManager();
            cacheManager = new CacheManager();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ProxyServer proxyServer = new ProxyServer(HTTP_PORT);
        proxyServer.listen();
    }

    /**
     * Listen and accept browser requests
     */
    @SuppressWarnings("InfiniteLoopStatement")
    private void listen() {
        System.out.println("[ProxyServer] Start listening... ");

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();

                System.out.println("[ProxyServer] Connection Accepted: " + clientSocket.getRemoteSocketAddress());
                Thread thread = new Thread(new RequestHandler(clientSocket, blockManager, cacheManager));
                thread.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
