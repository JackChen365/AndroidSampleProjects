package com.cz.android.simplehttp.io.thread;

import java.io.*;
import java.util.concurrent.CountDownLatch;

public class FileCopyWorker implements Runnable{
    private final CountDownLatch countDownLatch;
    private final File file;
    private final File dest;
    final long start;
    final long end;


    public FileCopyWorker(CountDownLatch countDownLatch,File file,File dest, long start, long end) {
        this.countDownLatch=countDownLatch;
        this.file=file;
        this.dest=dest;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        byte[] buffer=new byte[50*1024*1024];
        try(RandomAccessFile sourceFile=new RandomAccessFile(file,"r");
            RandomAccessFile randomAccessFile=new RandomAccessFile(dest,"rw");){
            int read;
            sourceFile.seek(start);
            randomAccessFile.seek(start);
            while(-1!=(read=sourceFile.read(buffer))){
                randomAccessFile.write(buffer,0,read);
                if(end <= randomAccessFile.getFilePointer()){
                    //Over the boundary
                    break;
                }
            }
            countDownLatch.countDown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
