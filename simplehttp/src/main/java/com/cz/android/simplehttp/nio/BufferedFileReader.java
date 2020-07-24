package com.cz.android.simplehttp.nio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BufferedFileReader {
    private static int defaultCharBufferSize = 10;
    private ByteBuffer byteBuffer;
    private FileChannel fileChannel;

    public static void main(String[] args) throws IOException {
        File file=new File("resources/request.txt");
        FileChannel channel = new FileInputStream(file).getChannel();
        BufferedFileReader bufferedFileReader = new BufferedFileReader(channel, 50);
        String line;
        boolean enterChunk=false;
        while(null!=(line = bufferedFileReader.readLine())){
            if(enterChunk||"\r\n".equals(line)){
                //The chunk size
                enterChunk=true;
                line = bufferedFileReader.readLine();
                if(null != line){
                    int size =  Integer.parseInt(line.trim(),16);
                    if(0 < size){
                        System.out.println("ChunkSize:"+size);
                        StringBuilder output=new StringBuilder();
                        ChunkSize chunkSize = new ChunkSize(0, 0,size);
                        while(0 < chunkSize.chunkSize){
                            bufferedFileReader.readChunk(bufferedFileReader,chunkSize);
                            ByteBuffer buffer = bufferedFileReader.getBuffer();
                            int remaining = chunkSize.getRemaining();
                            int position = buffer.position();
                            String s = new String(buffer.array(), position, chunkSize.getRemaining());
                            output.append(s);
                            buffer.position(position+remaining);
                        }
                        System.out.println(output);
                    }
                }
            }
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

    /**
     * Skip a certain offset. We won't use FileChannel.position(xx)
     * Because here this is just for SocketChannel.
     * @param num
     * @throws IOException
     */
    public void skip(FileChannel channel,int num) throws IOException {
        int remaining = byteBuffer.remaining();
        if(num < remaining){
            int position = byteBuffer.position();
            byteBuffer.position(position+num);
        } else {
            int read;
            num-=remaining;
            byteBuffer.clear();
            while(0 < num && 0 < (read = fileChannel.read(byteBuffer))){
                if(0 > num - read){
                    //Out of the boundary, Move to the new position.
                    byteBuffer.flip();
                    byteBuffer.position(num);
                    num=0;
                } else {
                    num-=read;
                    byteBuffer.clear();
                }
            }
        }
    }

    private void readChunk(BufferedFileReader channelReader,ChunkSize chunkSize) throws IOException {
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
