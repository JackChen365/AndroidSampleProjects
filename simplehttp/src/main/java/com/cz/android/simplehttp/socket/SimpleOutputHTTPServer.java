package com.cz.android.simplehttp.socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class SimpleOutputHTTPServer {

    public static void main(String args[]) throws IOException {

        ServerSocket server = new ServerSocket(8090);
        System.out.println("Listening for connection on port 8090 ....");
        while (true) {
            Socket clientSocket = server.accept();
            InputStream isr = clientSocket.getInputStream();
            try {
                int read;
                byte[] bytes=new byte[100*1024];
                FileOutputStream fileOutputStream=new FileOutputStream(new File("request.txt"));
                while (-1!=(read = isr.read(bytes))) {
                    fileOutputStream.write(bytes,0,read);
                    System.out.println("read:"+read+"\n"+new String(bytes,0,read));
                }
                OutputStream outputStream = clientSocket.getOutputStream();
                outputStream.write("response!".getBytes());
                outputStream.flush();
            } catch (SocketException e){
                clientSocket.close();
            }
        }
    }
}