package com.cz.android.simplehttp.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RequestHandler implements Runnable {

    /**
     * Browser input stream
     */
    private InputStream clientInputStream;

    /**
     * Browser output stream
     */
    private OutputStream clientOutputStream;

    /**
     * Manager classes
     */
    private BlockManager blockManager;
    private CacheManager cacheManager;


    /**
     * Constructor
     *
     * @param clientSocket browser socket
     * @param blockManager block manager
     * @param cacheManager cache manager
     */
    public RequestHandler(Socket clientSocket, BlockManager blockManager, CacheManager cacheManager) {
        try {
            this.clientInputStream = clientSocket.getInputStream();
            this.clientOutputStream = clientSocket.getOutputStream();

            this.blockManager = blockManager;
            this.cacheManager = cacheManager;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start handling client request
     */
    @Override
    public void run() {
        try {
            // Reading request from input stream
            String received = IORedirect.receiveString(clientInputStream);
            Request request = new Request(received);
            // Print http request header
            System.out.println(request.getMessage());

            // Send 404 response if the site is blocked
            if (blockManager.isBlocked(request.getHostName())) {
                String response = ResponseHelper.buildErrorResponse();
                IORedirect.sendString(response, clientOutputStream);
                close();

                System.out.println("[ProxyServer] Blocked: " + request.getHostName());

                return;
            }


            // Check request type
            if (request.isHttpsConnect()) {
                HttpsRedirect httpsRedirect = new HttpsRedirect(request, clientInputStream, clientOutputStream);
                httpsRedirect.start();
            } else {
                HttpRedirect httpRedirect = new HttpRedirect(request, clientOutputStream, cacheManager);
                httpRedirect.start();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        // Close client connection
        try {
            close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save cache and close client connection
     *
     * @throws IOException io error
     */
    private void close() throws IOException {
        cacheManager.saveCache();

        if (clientInputStream != null)
            clientInputStream.close();

        if (clientOutputStream != null)
            clientOutputStream.close();
    }
}
