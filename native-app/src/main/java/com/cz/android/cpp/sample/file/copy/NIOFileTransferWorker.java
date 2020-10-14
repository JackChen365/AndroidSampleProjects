package com.cz.android.cpp.sample.file.copy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.CountDownLatch;

public class NIOFileTransferWorker implements Runnable{
    private final CountDownLatch countDownLatch;
    private final File file;
    private final File dest;
    final long start;
    final long end;


    public NIOFileTransferWorker(CountDownLatch countDownLatch, File file, File dest, long start, long end) {
        this.countDownLatch=countDownLatch;
        this.file=file;
        this.dest=dest;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        int bufferSize=50*1024*1024;
        try(FileChannel inputChannel=new FileInputStream(file).getChannel();
            FileChannel outputChannel=new FileOutputStream(dest).getChannel()){
            long position=start;
            while(start<end){
                long transfer = inputChannel.transferTo(position,bufferSize, outputChannel);
                if(position+transfer >= end){
                    //Over the boundary
                    break;
                }
                position+=transfer;
            }
            countDownLatch.countDown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
