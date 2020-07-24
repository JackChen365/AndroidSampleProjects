package com.cz.android.simplehttp.stream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class BufferedFileChannel extends FileChannel {

    public static void main(String[] args) throws IOException {
        File file = new File("resources/report/template/export_dependency_tree.ftl");
        FileInputStream inputStream=new FileInputStream(file);
        FileChannel channel = inputStream.getChannel();

        BufferedFileChannel bufferedFileChannel=new BufferedFileChannel(channel,50);
        String line;
        while(null!=(line=bufferedFileChannel.readLine())){
            System.out.println(line);
        }
    }


    private static int defaultCharBufferSize = 8192;
    private ByteBuffer byteBuffer;

    private FileChannel fileChannel;

    public BufferedFileChannel(FileChannel fileChannel){
        this(fileChannel,defaultCharBufferSize);
    }

    public BufferedFileChannel(FileChannel fileChannel,int bufferSize) {
        if (bufferSize <= 0)
            throw new IllegalArgumentException("Buffer size <= 0");
        this.fileChannel = fileChannel;
        this.byteBuffer=ByteBuffer.allocate(bufferSize);
    }

    /** Checks to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
        if (fileChannel == null)
            throw new IOException("Stream closed");
    }

    /**
     * Read file channel by a internal buffer
     * @return
     * @throws IOException
     */
    private String readLine() throws IOException {
        ensureOpen();
        String line;
        String remainString=null;
        //First time when we read the line.
        if(0 == fileChannel.position()){
            fileChannel.read(byteBuffer);
            byteBuffer.flip();
        }
        while(byteBuffer.hasRemaining()&&null!=(line=readByteBuffer(byteBuffer))){
            if(!byteBuffer.hasRemaining()){
                //We use all the buffer, trying to read more byte to the buffer.
                if(null!=remainString){
                    remainString += line;
                } else {
                    remainString = line;
                }
                byteBuffer.clear();
                //Continue to read
                if(0 > fileChannel.read(byteBuffer)){
                    byteBuffer.position(byteBuffer.limit());
                    return line;
                }
                byteBuffer.flip();
            }
            if ('\n' == line.charAt(line.length() - 1)) {
                //End with line feeds, just return the line.
                if(null!=remainString){
                    return remainString+line.trim();
                } else {
                    return line.trim();
                }
            }
        }
        return null;
    }

    private String readByteBuffer(ByteBuffer byteBuffer) throws EOFException{
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

    @Override
    public int read(ByteBuffer dst) throws IOException {
        ensureOpen();
        return fileChannel.read(dst);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        ensureOpen();
        return fileChannel.read(dsts,offset,length);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        ensureOpen();
        return fileChannel.write(src);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        ensureOpen();
        return fileChannel.write(srcs,offset,length);
    }

    @Override
    public long position() throws IOException {
        ensureOpen();
        return fileChannel.position();
    }

    @Override
    public FileChannel position(long newPosition) throws IOException {
        ensureOpen();
        return fileChannel.position(newPosition);
    }

    @Override
    public long size() throws IOException {
        ensureOpen();
        return fileChannel.size();
    }

    @Override
    public FileChannel truncate(long size) throws IOException {
        ensureOpen();
        return fileChannel.truncate(size);
    }

    @Override
    public void force(boolean metaData) throws IOException {
        ensureOpen();
        fileChannel.force(metaData);
    }

    @Override
    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        ensureOpen();
        return fileChannel.transferTo(position,count,target);
    }

    @Override
    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        ensureOpen();
        return fileChannel.transferFrom(src,position,count);
    }

    @Override
    public int read(ByteBuffer dst, long position) throws IOException {
        ensureOpen();
        return fileChannel.read(dst,position);
    }

    @Override
    public int write(ByteBuffer src, long position) throws IOException {
        ensureOpen();
        return fileChannel.write(src,position);
    }

    @Override
    public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
        ensureOpen();
        return fileChannel.map(mode,position,size);
    }

    @Override
    public FileLock lock(long position, long size, boolean shared) throws IOException {
        ensureOpen();
        return fileChannel.lock();
    }

    @Override
    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        ensureOpen();
        return fileChannel.tryLock();
    }

    @Override
    protected void implCloseChannel() throws IOException {
        ensureOpen();
    }
}
