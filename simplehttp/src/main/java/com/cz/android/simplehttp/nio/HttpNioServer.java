package com.cz.android.simplehttp.nio;

import com.okay.java.sample.http.HttpUrlConnectionClient;

import javax.activation.MimetypesFileTypeMap;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * The simplest http server. Only support the method: GET.
 * Cooperate with the class {@link HttpUrlConnectionClient} which is the client use {@link java.net.HttpURLConnection} fetch something from this server.
 */
public class HttpNioServer {
    private static final String METHOD_GET="GET";
    private static final String METHOD_POST="POST";
    private static final MimetypesFileTypeMap fileTypeMap=new MimetypesFileTypeMap();
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    private int requestCount=0;

    public static void main(String[] args) throws IOException {
        HttpNioServer httpNioServer = new HttpNioServer();
        httpNioServer.startServer();
    }

    public void startServer(){
        try(ServerSocketChannel serverSocketChannel=ServerSocketChannel.open()){
            InetSocketAddress socketAddress = new InetSocketAddress("localhost", 8090);
            serverSocketChannel.bind(socketAddress);
            serverSocketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Start the server!");
            while(true){
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while(iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    if(selectionKey.isAcceptable()){
                        acceptChanel(selector, selectionKey);
                    } else if(selectionKey.isReadable()){
                        //Read something from channel.
                        response(selectionKey);
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptChanel(Selector selector, SelectionKey selectionKey) throws IOException {
        ServerSocketChannel channel= (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = channel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector,SelectionKey.OP_READ);
    }

    private void response(SelectionKey selectionKey) throws IOException {
        SocketChannel channel= (SocketChannel) selectionKey.channel();
        byteBuffer.clear();
        int read = -1;
        try {
            read = channel.read(byteBuffer);
        } catch (IOException e){
            channel.close();
            System.err.println("The connection interrupted!");
        }
        if(0 < read){
            byteBuffer.flip();
            String result = new String(byteBuffer.array(), 0, read);
            String lineSeparator = "\r\n";
            String[] requestsLines = result.split(lineSeparator);
            String[] requestLine = requestsLines[0].split(" ");
            String method = requestLine[0];
            String path = requestLine[1];
            String version = requestLine[2];
            String host = requestsLines[1].split(" ")[1];

            Map<String,String> headers = new HashMap<>();
            for (int h = 2; h < requestsLines.length; h++) {
                String header = requestsLines[h];
                if(0 < header.trim().length()){
                    String[] strings = header.split(": ");
                    headers.put(strings[0],strings[1]);
                }
            }

            boolean isKeepAlive = Boolean.valueOf(headers.get("keep-alive"));
            if(METHOD_GET.equalsIgnoreCase(method)){
                Path filePath = getFilePath(path);
                if (Files.exists(filePath)) {
                    SocketAddress remoteAddress = channel.getRemoteAddress();
                    System.out.println("Response client:"+remoteAddress+" process:"+(requestCount++)+" times");
                    String contentType = fileTypeMap.getContentType(filePath.toFile());
                    sendResponse(channel,"200 OK",contentType,Files.readAllBytes(filePath),isKeepAlive);
                } else if(METHOD_POST.equalsIgnoreCase(method)){
                    handlePostMethod(channel);
                } else {
                    // 404
                    byte[] notFoundContent = "<h1>Not found :(</h1>".getBytes();
                    sendResponse(channel, "404 Not Found", "text/html", notFoundContent,isKeepAlive);
                }
            }
        }
    }

    private Path getFilePath(String path) {
        if ("/".equals(path)) {
            path = "/index.html";
        }
        File file=new File("resources/report");
        return Paths.get(file.getAbsolutePath(), path);
    }

    private void sendResponse(SocketChannel channel, String method, String contentType, byte[] content,boolean isKeepAlive) throws IOException {
        String lineSeparator = "\r\n";
        byteBuffer.clear();
        byteBuffer.put(("HTTP/1.1"+" "+method + lineSeparator).getBytes());
        byteBuffer.put(("ContentType: " + contentType + lineSeparator).getBytes());
        byteBuffer.put(("Content-Length: " + content.length + lineSeparator).getBytes());
        byteBuffer.put(("keep-alive: " + "true" + lineSeparator).getBytes());
        byteBuffer.put(lineSeparator.getBytes());
        byteBuffer.put(content);
        byteBuffer.put((lineSeparator).getBytes());
        byteBuffer.flip();
        channel.write(byteBuffer);
        byteBuffer.clear();
//        if(isKeepAlive){
//            channel.close();
//        }
    }

    private void handlePostMethod(SocketChannel channel) throws IOException {
        byteBuffer.clear();
        try {
            int read = -1;
            while(-1 != (read = channel.read(byteBuffer))){
                read = channel.read(byteBuffer);
                String s = readAsciiLine(byteBuffer);
                System.out.println(s);
            }
        } catch (IOException e){
            channel.close();
            System.err.println("The connection interrupted!");
        }
    }

    public static String readAsciiLine(ByteBuffer buffer) throws IOException {
        // TODO: support UTF-8 here instead
        StringBuilder result = new StringBuilder(80);
        while (true) {
            int c = buffer.get();
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
}
