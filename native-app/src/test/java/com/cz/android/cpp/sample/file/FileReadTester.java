package com.cz.android.cpp.sample.file;

import com.cz.android.cpp.sample.file.read.BufferedFileChannel;

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Created by cz
 * @date 2020/9/8 10:55 AM
 * @email bingo110@126.com
 */
public class FileReadTester {

    @Test
    public void fileReadTest() throws IOException {
        File file=new File("/Users/cz/Downloads/Doom.mkv");
        long st = System.currentTimeMillis();
        //The first one test the normal operation of read file.
        test1(file,100*1024);
        long time1=(System.currentTimeMillis()-st);
        st = System.currentTimeMillis();
        //The second one test the nio operation of read file.
        test2(file,100*1024);
        long time2=(System.currentTimeMillis()-st);
        st = System.currentTimeMillis();
        //The third one test from native read file.
        long time3=(System.currentTimeMillis()-st);
        st = System.currentTimeMillis();
    }

    private void test1(File file,int bufferSize) throws IOException {
        byte[] buffer=new byte[bufferSize];
        try(BufferedInputStream reader=new BufferedInputStream(new FileInputStream(file))){
            int len;
            while(0 != (len=reader.read(buffer))){
            }
        }
    }

    private void test2(File file, int bufferSize) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        FileChannel channel = fileInputStream.getChannel();
        BufferedFileChannel bufferedFileChannel=null;
        try {
            bufferedFileChannel = new BufferedFileChannel(channel);
            ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
            int len;
            while(0 != (len=bufferedFileChannel.read(byteBuffer))){
            }
        } finally {
            if(null!=bufferedFileChannel){
                bufferedFileChannel.close();
            }
        }
    }
}
