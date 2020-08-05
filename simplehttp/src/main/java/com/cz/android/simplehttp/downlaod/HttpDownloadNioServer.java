package com.cz.android.simplehttp.downlaod;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * The simplest http server. Only support the method: GET.HEAD
 * Cooperate with the class {@link HttpURLConnection} which is the client use {@link HttpURLConnection} fetch something from this server.
 */
public class HttpDownloadNioServer {
    private static final String METHOD_GET="GET";
    private static final String METHOD_HEAD="HEAD";
    private static final String LINE_FEEDS = "\r\n";
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer largeByteBuffer;
    private int requestCount=0;

    public static void main(String[] args) {
        HttpDownloadNioServer httpNioServer = new HttpDownloadNioServer();
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
                    iterator.remove();
                    try {
                        if(selectionKey.isConnectable()){
                            SocketChannel channel= (SocketChannel) selectionKey.channel();
                            if(channel.finishConnect()){
                                channel.close();
                            }
                        } else if(selectionKey.isAcceptable()){
                            acceptChanel(selector, selectionKey);
                        } else if(selectionKey.isReadable()){
                            //Read something from channel.
                            SocketChannel channel= (SocketChannel) selectionKey.channel();
                            response(channel);
                        } else if(selectionKey.isWritable()){
                            //Write
                        }
                    } catch (IOException e) {
                        System.err.println("The channel interrupted!");
                        SelectableChannel channel = selectionKey.channel();
                        channel.close();
                    }
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
        socketChannel.register(selector,SelectionKey.OP_READ|SelectionKey.OP_WRITE);
    }

    private void response(SocketChannel channel) throws IOException {
        DownloadBufferedChannelReader channelReader = new DownloadBufferedChannelReader(channel,100);
        String protocolLine = channelReader.readLine();
        if(null!=protocolLine){
            String[] requestLine = protocolLine.trim().split(" ");
            String method = requestLine[0];
            String path = requestLine[1];
            String version = requestLine[2];

            String headerLine;
            Map<String,String> headers = new HashMap<>();
            while(!LINE_FEEDS.equals(headerLine=channelReader.readLine())){
                if(0 < headerLine.trim().length()){
                    String[] strings = headerLine.split(": ");
                    headers.put(strings[0],strings[1].trim());
                }
            }
            boolean isKeepAlive = Boolean.valueOf(headers.get("keep-alive"));
            if(METHOD_GET.equalsIgnoreCase(method)){
                Path filePath = getFilePath(path);
                if (Files.exists(filePath)) {
                    SocketAddress remoteAddress = channel.getRemoteAddress();
                    System.out.println("Response client:"+remoteAddress+" process:"+(requestCount++)+" times");
                    File file = filePath.toFile();
                    String contentType = URLConnection.guessContentTypeFromName(file.getName());
                    String range = headers.get("Range");
                    long start=0;
                    long end=file.length();
                    if(null!=range){
                        String[] splitArray = range.substring("bytes:".length()).split("-");
                        start = Long.valueOf(splitArray[0]);
                        end = Long.valueOf(splitArray[1]);
                    }
                    outputDownloadFile(channel,file,contentType,start,end);
                } else {
                    // 404
                    byte[] notFoundContent = "<h1>Not found :(</h1>".getBytes();
                    sendResponse(channel, "404 Not Found", "text/html", notFoundContent,notFoundContent.length,isKeepAlive);
                }
            } else if(METHOD_HEAD.equalsIgnoreCase(method)){
                Path filePath = getFilePath(path);
                if (Files.exists(filePath)) {
                    File file = filePath.toFile();
                    String contentType = URLConnection.guessContentTypeFromName(file.getName());
                    //We won't really return the content back.
                    sendResponse(channel,"200 OK",contentType,null,file.length(),isKeepAlive);
                } else {
                    // 404
                    byte[] notFoundContent = "<h1>Not found :(</h1>".getBytes();
                    sendResponse(channel, "404 Not Found", "text/html", notFoundContent,notFoundContent.length,isKeepAlive);
                }
            }
        }
    }

    private void outputDownloadFile(SocketChannel channel,File file,String contentType, long start, long end) throws IOException {
        String lineSeparator = "\r\n";
        byteBuffer.clear();
        byteBuffer.put(("HTTP/1.1"+" "+ HttpURLConnection.HTTP_PARTIAL+" OK" + lineSeparator).getBytes());
        byteBuffer.put(("Content-Type: " + contentType + lineSeparator).getBytes());
        byteBuffer.put(("Content-Length: " + (end-start) + lineSeparator).getBytes());
        byteBuffer.put(("keep-alive: " + "true" + lineSeparator).getBytes());
        byteBuffer.put(lineSeparator.getBytes());
        byteBuffer.flip();
        channel.write(byteBuffer);
        if(null==largeByteBuffer){
             largeByteBuffer = ByteBuffer.allocate(10*1024*1024);
        }
        largeByteBuffer.clear();
        try(FileChannel fileChannel=new FileInputStream(file).getChannel()){
            int read=0;
            long offset=start;
            fileChannel.position(offset);
            while(fileChannel.position() <= end&&-1!=(read=fileChannel.read(largeByteBuffer))){
                if(offset + largeByteBuffer.capacity() > end){
                    largeByteBuffer.limit((int) (end-offset));
                }
                largeByteBuffer.flip();
                while(largeByteBuffer.hasRemaining()){
                    offset+=channel.write(largeByteBuffer);
                }
                largeByteBuffer.clear();
            }
            System.out.println("Read size:"+read);
        }
        System.out.println("Send completed!");
        byteBuffer.clear();
        byteBuffer.put((lineSeparator+lineSeparator).getBytes());
        byteBuffer.flip();
        channel.write(byteBuffer);
        channel.close();
    }

    private Path getFilePath(String path) throws UnsupportedEncodingException {
        if ("/".equals(path)) {
            path = "/index.html";
        }
        File file = new File(".", URLDecoder.decode(path, StandardCharsets.UTF_8.toString()));
        return file.toPath();
    }

    private void sendResponse(SocketChannel channel, String status, String contentType, byte[] content,long contentLength,boolean isKeepAlive) throws IOException {
        String feedLines = "\r\n";
        byteBuffer.clear();
        byteBuffer.put(("HTTP/1.1"+" "+status + feedLines).getBytes());
        byteBuffer.put(("Content-Type: " + contentType + feedLines).getBytes());
        byteBuffer.put(("Content-Length: " + contentLength + feedLines).getBytes());
        byteBuffer.put(("keep-alive: " + "true" + feedLines).getBytes());
        byteBuffer.put(feedLines.getBytes());
        if(null!=content){
            byteBuffer.put(content);
        }
        byteBuffer.put((feedLines+feedLines).getBytes());
        byteBuffer.flip();
        channel.write(byteBuffer);
        byteBuffer.clear();
        channel.close();
//        if(isKeepAlive){
//        }
    }
}
