package com.cz.android.simplehttp.pool;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class HttpConnectionPoolTest {
    public static void main(String[] args) throws IOException {
        ConnectionPool connectionPool = ConnectionPool.getDefault();
        Connection connection = connectionPool.get("localhost", 8090);
        if(null==connection){
            connection=new Connection();
            connection.connect("localhost", 8090,100,100);
        }
        Socket socket = connection.getSocket();
        OutputStream outputStream = socket.getOutputStream();


    }

    private void sendRequest(Socket socket, String status, String contentType, byte[] content, boolean isKeepAlive) throws IOException {
        String lineSeparator = "\r\n";
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(("HTTP/1.1 "+ status + lineSeparator).getBytes());
        outputStream.write(("ContentType: " + contentType + lineSeparator).getBytes());
        outputStream.write(lineSeparator.getBytes());
        outputStream.write(content);
        outputStream.write((lineSeparator+lineSeparator).getBytes());
        outputStream.flush();
    }
}
