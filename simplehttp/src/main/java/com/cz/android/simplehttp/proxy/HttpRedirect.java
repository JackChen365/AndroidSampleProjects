package com.cz.android.simplehttp.proxy;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRedirect {

    /**
     * Browser request
     */
    private Request request;

    /**
     * Browser write end
     */
    private OutputStream clientOutputStream;

    /**
     * Cache manager
     */
    private CacheManager cacheManager;


    /**
     * Constructor
     *
     * @param request            browser request
     * @param clientOutputStream browser output stream
     * @param cacheManager       cache manager
     */
    public HttpRedirect(Request request, OutputStream clientOutputStream, CacheManager cacheManager) {
        this.request = request;
        this.clientOutputStream = clientOutputStream;
        this.cacheManager = cacheManager;
    }

    /**
     * Start redirecting HTTP request between the browser client and the remote server
     * If the requested file is being cached, send back the local cached file
     * If no cache is found, download from the remote server
     *
     * @throws IOException io error
     */
    public void start() throws IOException {
        if (cacheManager.isCached(request)) {
            // Get local output stream
            FileInputStream localInputStream = cacheManager.getLocalInputStream(request);

            // Send response to client
            String response = ResponseHelper.buildSuccessResponse();
            IORedirect.sendString(response, clientOutputStream);

            System.out.println("[ProxyServer] Using cache for " + request.getURL());

            // Transfer payload
            IORedirect.transfer(localInputStream, clientOutputStream);


            // Close input stream
            localInputStream.close();

        } else {
            // Create connection to the remote server
            URL url = new URL(request.getURL());
            HttpURLConnection remoteConn = (HttpURLConnection) url.openConnection();
            System.out.println("[ProxyServer] response for " + request.getURL());
            if (remoteConn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                // Get streams
                InputStream remoteInputStream = remoteConn.getInputStream();
                FileOutputStream localOutputStream = cacheManager.getLocalOutputStream(request);

                // Send response to client
                String response = ResponseHelper.buildResponse(
                        remoteConn.getResponseCode(),
                        remoteConn.getResponseMessage()
                );
                IORedirect.sendString(response, clientOutputStream);

                // Transfer payload
                IORedirect.transfer(remoteInputStream, clientOutputStream, localOutputStream);
                cacheManager.addCache(request);

                // Close input stream
                remoteInputStream.close();
            } else {
                String response = ResponseHelper.buildResponse(
                        remoteConn.getResponseCode(),
                        remoteConn.getResponseMessage()
                );
                IORedirect.sendString(response, clientOutputStream);
            }

            remoteConn.disconnect();
        }
    }
}
