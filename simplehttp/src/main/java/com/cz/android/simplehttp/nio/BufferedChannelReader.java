package com.cz.android.simplehttp.nio;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class BufferedChannelReader {
    private static int defaultCharBufferSize = 1024;
    private ByteBuffer byteBuffer;
    private SocketChannel socketChannel;

    public BufferedChannelReader(SocketChannel socketChannel) {
        this(socketChannel,defaultCharBufferSize);
    }

    public BufferedChannelReader(SocketChannel socketChannel, int bufferSize) {
        this.socketChannel = socketChannel;
        this.byteBuffer=ByteBuffer.allocate(bufferSize);
        this.byteBuffer.position(byteBuffer.limit());
    }

    /** Checks to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
        if (socketChannel == null)
            throw new IOException("Stream closed");
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public int readBuffer()throws IOException{
        byteBuffer.clear();
        int read = socketChannel.read(byteBuffer);
        byteBuffer.flip();
        return read;
    }

    public ByteBuffer getBuffer(){
        return byteBuffer;
    }

    public byte read() throws IOException {
        ensureOpen();
        if(!byteBuffer.hasRemaining()){
            socketChannel.read(byteBuffer);
            byteBuffer.flip();
        }
        return byteBuffer.get();
    }

    public void readChunk(BufferedChannelReader channelReader, HttpNioServer.ChunkSize chunkSize) throws IOException {
        ByteBuffer byteBuffer = channelReader.getBuffer();
        int remaining = byteBuffer.remaining();
        if(chunkSize.chunkSize < remaining){
            chunkSize.start = byteBuffer.position();
            chunkSize.end=chunkSize.start+chunkSize.chunkSize;
            chunkSize.chunkSize=0;
        } else if(!byteBuffer.hasRemaining()){
            int read=channelReader.readBuffer();
            if(0 > chunkSize.chunkSize - read){
                //Out of the boundary, Move to the new position.
                chunkSize.start=0;
                chunkSize.end=chunkSize.chunkSize;
                chunkSize.chunkSize=0;
            } else {
                chunkSize.chunkSize-=read;
                chunkSize.start=0;
                chunkSize.end=byteBuffer.limit();
            }
        } else {
            chunkSize.start=byteBuffer.position();
            chunkSize.end=byteBuffer.limit();
            chunkSize.chunkSize-=byteBuffer.remaining();
        }
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
            socketChannel.read(byteBuffer);
            byteBuffer.flip();
        }
        while(byteBuffer.hasRemaining()&&null!=(line=readByteBuffer(byteBuffer))){
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
                if(0 > socketChannel.read(byteBuffer)){
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

    private String readByteBuffer(ByteBuffer byteBuffer) throws EOFException {
        StringBuilder result = new StringBuilder(80);
        while (byteBuffer.hasRemaining()) {
            char c = (char) byteBuffer.get();
            result.append(c);
            if (c == -1) {
                throw new EOFException();
            } else if (c == '\n') {
                break;
            }
        }
        return result.toString();
    }

}
