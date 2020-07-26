package com.cz.android.simplehttp.upload;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BufferedFileReader {
    private static int defaultCharBufferSize = 10;
    private ByteBuffer byteBuffer;
    private FileChannel fileChannel;

    public static void main(String[] args) throws IOException {
        File file=new File("resources/form_request.txt");
        FileChannel channel = new FileInputStream(file).getChannel();
        BufferedFileReader bufferedFileReader = new BufferedFileReader(channel, 10);
        String line;
        while(null!=(line= bufferedFileReader.readLine())){
            System.out.print(line);
        }
    }

    public BufferedFileReader(FileChannel fileChannel) {
        this(fileChannel,defaultCharBufferSize);
    }

    public BufferedFileReader(FileChannel fileChannel, int bufferSize) {
        this.fileChannel = fileChannel;
        this.byteBuffer=ByteBuffer.allocate(bufferSize);
        this.byteBuffer.limit(0);
    }

    /** Checks to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
        if (fileChannel == null)
            throw new IOException("Stream closed");
    }

    public int readBuffer()throws IOException{
        byteBuffer.clear();
        int read = fileChannel.read(byteBuffer);
        byteBuffer.flip();
        return read;
    }

    public ByteBuffer getBuffer(){
        return byteBuffer;
    }

    public byte read() throws IOException {
        ensureOpen();
        if(!byteBuffer.hasRemaining()){
            fileChannel.read(byteBuffer);
            byteBuffer.flip();
        }
        return byteBuffer.get();
    }

    /**
     * Read file channel by a internal buffer
     * @return
     * @throws IOException
     */
    public String readLine() throws IOException {
        ensureOpen();
        String line;
        String remainString=null;
        //First time when we read the line.
        if(!byteBuffer.hasRemaining()){
            byteBuffer.clear();
            fileChannel.read(byteBuffer);
            byteBuffer.flip();
        }
        while(byteBuffer.hasRemaining()&&null!=(line= readLine(byteBuffer))){
            if ('\n' == line.charAt(line.length() - 1)) {
                //End with line feeds, just return the line.
                if(null!=remainString){
                    return remainString+line;
                } else {
                    return line;
                }
            }
            if(!byteBuffer.hasRemaining()){
                if(null!=remainString){
                    remainString += line;
                } else {
                    remainString = line;
                }
                byteBuffer.clear();
                //Nothing to read.
                if(0 > fileChannel.read(byteBuffer)){
                    byteBuffer.position(byteBuffer.limit());
                    if(null!=remainString){
                        return remainString;
                    } else {
                        return line;
                    }
                }
                byteBuffer.flip();
            }
        }
        return null;
    }

    private String readLine(ByteBuffer byteBuffer) throws EOFException {
        return readLine(byteBuffer,byteBuffer.limit());
    }

    private String readLine(ByteBuffer byteBuffer,int limit) throws EOFException {
        StringBuilder result = null;
        while (byteBuffer.position()<=limit&&byteBuffer.hasRemaining()) {
            char c = (char) byteBuffer.get();
            if(null==result){
                result = new StringBuilder(80);
            }
            result.append(c);
            if (c == -1) {
                throw new EOFException();
            } else if (c == '\n') {
                break;
            }
        }
        if(null==result){
            return null;
        } else {
            return result.toString();
        }
    }
}
