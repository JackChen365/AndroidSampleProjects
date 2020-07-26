package com.cz.android.simplehttp.upload;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BufferedChunkFileReader {
    private static final String LINE_FEEDS = "\r\n";

    private static int defaultCharBufferSize = 10;
    private ByteBuffer byteBuffer;
    private FileChannel fileChannel;
    private boolean startDetectStreamChunk;
    private ChunkSize chunkSize;

    public static void main(String[] args) throws IOException {
        File file=new File("resources/request.txt");
        FileChannel channel = new FileInputStream(file).getChannel();
        BufferedChunkFileReader bufferedFileReader = new BufferedChunkFileReader(channel, 50);
        String line;
        while(null!=(line= bufferedFileReader.readLine())){
            System.out.print(line);
        }
    }

    public BufferedChunkFileReader(FileChannel fileChannel) {
        this(fileChannel,defaultCharBufferSize);
    }

    public BufferedChunkFileReader(FileChannel fileChannel, int bufferSize) {
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

    /**
     * Tips.
     * Not thread safe.
     * @return
     * @throws IOException
     */
    public String readLine() throws IOException{
        if(null==chunkSize&&!startDetectStreamChunk){
            String line = readLineInternal();
            //next line is chunk size.
            if(LINE_FEEDS.equals(line)){
                startDetectStreamChunk =true;
            }
            return line;
        } else {
            if (startDetectStreamChunk) {
                String line = readLineInternal();
                if(LINE_FEEDS.equals(line)){
                    return line;
                } else {
                    startDetectStreamChunk = false;
                    //Start to detect the new chunk of the file.
                    int size = Integer.parseInt(line.trim(), 16);
                    if (0 < size) {
                        //Because the size include the CRLF
                        chunkSize = new ChunkSize(0,0, size);
                        readBufferChunk(chunkSize);
                    }
                }
            }
            String line = readLineWithinChunk();
            if(0 == chunkSize.chunkSize){
                //The work of this file chunk is over. next time we will start to detect the new chunk of the file.
                startDetectStreamChunk =true;
            }
            return line;
        }
    }

    private String readLineWithinChunk() throws IOException {
        String line=null;
        String remainString=null;
        int consumed = 0;
        int position = byteBuffer.position();
        while (0 < chunkSize.chunkSize) {
            while(byteBuffer.hasRemaining()&&null!=(line = readLine(byteBuffer, chunkSize.end))){
                consumed += byteBuffer.position()-position;
                if (0==chunkSize.chunkSize-consumed||'\n' == line.charAt(line.length() - 1)) {
                    chunkSize.chunkSize-=consumed;
                    if(null==remainString){
                        return line;
                    } else {
                        return remainString+line;
                    }
                } else {
                    if(null==remainString){
                        remainString=line;
                    } else {
                        remainString+=line;
                    }
                }
            }
            readBufferChunk(chunkSize);
            position = byteBuffer.position();
        }
        return line;
    }

    private void skipChunk(ChunkSize chunkSize) throws IOException {
        ByteBuffer buffer = getBuffer();
        int position = buffer.position();
        int remaining = chunkSize.getRemaining();
        while (0 < chunkSize.chunkSize) {
            readBufferChunk(chunkSize);
        }
        buffer.position(position+remaining);
    }

    private void readBufferChunk(ChunkSize chunkSize) throws IOException {
        ByteBuffer byteBuffer = getBuffer();
        int remaining = byteBuffer.remaining();
        if(chunkSize.chunkSize < remaining){
            chunkSize.start = byteBuffer.position();
            chunkSize.end=chunkSize.start+chunkSize.chunkSize;
        } else if(!byteBuffer.hasRemaining()){
            int read = readBuffer();
            if(0 > chunkSize.chunkSize - read){
                //Out of the boundary, Move to the new position.
                chunkSize.start=0;
                chunkSize.end=chunkSize.chunkSize;
            } else {
                chunkSize.start=0;
                chunkSize.end=byteBuffer.limit();
            }
        } else {
            chunkSize.start=byteBuffer.position();
            chunkSize.end=byteBuffer.limit();
        }
    }

    private ByteBuffer getBuffer(){
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
    private String readLineInternal() throws IOException {
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
        while (byteBuffer.position()<limit&&byteBuffer.hasRemaining()) {
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

    static class ChunkSize{
        int start;
        int end;
        int chunkSize;

        public ChunkSize(int start, int end,int chunkSize) {
            this.start = start;
            this.end=end;
            this.chunkSize = chunkSize;
        }

        public int getRemaining(){
            return end-start;
        }
    }
}
