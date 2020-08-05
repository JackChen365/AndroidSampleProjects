package com.cz.android.simplehttp.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class HttpsRedirect {

    /**
     * Browser request
     */
    private Request request;

    /**
     * Browser read end
     */
    private InputStream clientInputStream;

    /**
     * Browser write end
     */
    private OutputStream clientOutputStream;


    /**
     * Constructor
     *
     * @param request            browser request
     * @param clientInputStream  browser input stream
     * @param clientOutputStream browser output stream
     */
    public HttpsRedirect(Request request, InputStream clientInputStream, OutputStream clientOutputStream) {
        this.request = request;
        this.clientInputStream = clientInputStream;
        this.clientOutputStream = clientOutputStream;
    }

    /**
     * Start redirecting HTTPS request between the browser client and the remote server
     * The browser first sends a HTTP CONNECT request to the proxy server with the url of the
     * remote server. The proxy will serve as a middlemen and try to establish a connection
     * with the remote server. Once the connection is established, connect the browser client
     * and the remote server by linking their input and output streams.
     *
     * @throws IOException          io error
     * @throws InterruptedException interrupted error
     */
    public void start() throws IOException, InterruptedException {
        // Translate host name to ip address
        InetAddress address = InetAddress.getByName(request.getHostName());

        Socket remoteSocket = new Socket(address, request.getPortNo());

        if (remoteSocket.isConnected()) {
            // Send success response
            String response = ResponseHelper.buildSuccessResponse();
            IORedirect.sendString(response, clientOutputStream);


            // Get remote streams
            final InputStream remoteInputStream = remoteSocket.getInputStream();
            OutputStream remoteOutputStream = remoteSocket.getOutputStream();


            // Transfer from server to client
            Thread incoming = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        IORedirect.transfer(remoteInputStream, clientOutputStream);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            incoming.start();


            // Transfer from client to server
            IORedirect.transfer(clientInputStream, remoteOutputStream);

            incoming.join();

            remoteInputStream.close();
            remoteOutputStream.close();

        } else {
            // Send error response
            String response = ResponseHelper.buildErrorResponse();
            IORedirect.sendString(response, clientOutputStream);
        }

        remoteSocket.close();
    }
}
