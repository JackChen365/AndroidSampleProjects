package com.cz.android.simplehttp.io.thread;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FileCopyTester {
    public static void main(String[] args) throws IOException, InterruptedException {
        long st = System.currentTimeMillis();
        File file=new File("resources/media/Doom Annihilation.mkv");

        //1. Copy file use normal stream API with single thread.
        File file1=new File("movie1.mkv");
        copyFile1(file,file1);
        long time1=System.currentTimeMillis()-st;
        System.out.println("The first method cost time:"+time1);
        st=System.currentTimeMillis();

        //2. Copy file use NIO with single thread.
        File file2=new File("movie2.mkv");
        copyFile2(file,file2);
        long time2=System.currentTimeMillis()-st;
        System.out.println("The second method cost time:"+time2);

        //3. Copy file use the RandomAccessFile with multi-thread
        st=System.currentTimeMillis();
        File file3=new File("movie3.mkv");
        copyFile3(file,file3);
        long time3=System.currentTimeMillis()-st;
        System.out.println("The third method cost time:"+time3);

        //4. Copy file use the NIO transfer API with multi-thread
        st=System.currentTimeMillis();
        File file4=new File("movie4.mkv");
        copyFile4(file,file4);
        long time4=System.currentTimeMillis()-st;
        System.out.println("The fourth method cost time:"+time4);
    }

    /**
     * Copy file use normal stream API with single thread.
     * @param target
     * @param dest
     * @throws IOException
     */
    private static void copyFile1(File target,File dest) throws IOException {
        try(FileInputStream fileInputStream=new FileInputStream(target);
            FileOutputStream fileOutputStream=new FileOutputStream(dest)){
            int read;
            byte[] buffer=new byte[50*1024*1024];
            while(-1!=(read=fileInputStream.read(buffer))){
                fileOutputStream.write(buffer,0,read);
                fileOutputStream.flush();
            }
        }
    }

    /**
     * Copy file use NIO with single thread.
     * @param target
     * @param dest
     * @throws IOException
     */
    private static void copyFile2(File target,File dest) throws IOException {
        try(FileInputStream fileInputStream=new FileInputStream(target);
            FileOutputStream fileOutputStream=new FileOutputStream(dest)){
            FileChannel inputChannel = fileInputStream.getChannel();
            FileChannel outputChannel = fileOutputStream.getChannel();
            int position=0;
            int bufferSize = 50 * 1024 * 1024;
            while(position<target.length()){
                long transfer = inputChannel.transferTo(position, bufferSize, outputChannel);
                if(transfer<=0){
                    break;
                }
                position+=bufferSize;
            }
            //Copy the remain size
            if(position<target.length()){
                inputChannel.transferTo(position,target.length()-position,outputChannel);
            }
        }
    }

    /**
     * Copy file use the RandomAccessFile with multi-thread
     * @param file
     * @param dest
     */
    private static void copyFile3(File file, File dest) throws InterruptedException {
        long fileLength = file.length();
        int availableProcessors = Runtime.getRuntime().availableProcessors() + 1;
        //Split the file into pieces.
        long remainSize = 0;
        long filePieceSize = fileLength / availableProcessors;
        //If we still have remain size after splitting.
        if(filePieceSize*availableProcessors!=fileLength){
            remainSize = fileLength - (filePieceSize * availableProcessors);
        }
        int start=0;
        CountDownLatch countDownLatch=new CountDownLatch(availableProcessors);
        try {
            ThreadPoolExecutor executorService = new ThreadPoolExecutor(availableProcessors, availableProcessors, 1L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
            executorService.allowCoreThreadTimeOut(true);
            for(int i=0;i<availableProcessors;i++){
                FileCopyWorker fileCopyWorker;
                if(i != availableProcessors-1){
                    fileCopyWorker = new FileCopyWorker(countDownLatch, file, dest, start, start + filePieceSize);
                } else {
                    fileCopyWorker = new FileCopyWorker(countDownLatch, file, dest, start, start + filePieceSize + remainSize);
                }
                executorService.execute(fileCopyWorker);
                start+=filePieceSize;
            }
        } finally {
            countDownLatch.await();
        }
    }

    /**
     * Copy file use the NIO API with multi-thread
     * @param file
     * @param dest
     */
    private static void copyFile4(File file, File dest) throws InterruptedException {
        long fileLength = file.length();
        int availableProcessors = Runtime.getRuntime().availableProcessors() + 1;
        //Split the file into pieces.
        long remainSize = 0;
        long filePieceSize = fileLength / availableProcessors;
        //If we still have remain size after splitting.
        if(filePieceSize*availableProcessors!=fileLength){
            remainSize = fileLength - (filePieceSize * availableProcessors);
        }
        int start=0;
        CountDownLatch countDownLatch=new CountDownLatch(availableProcessors);
        try {
            ThreadPoolExecutor executorService = new ThreadPoolExecutor(availableProcessors, availableProcessors, 1L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
            executorService.allowCoreThreadTimeOut(true);
            for(int i=0;i<availableProcessors;i++){
                NIOFileTransferWorker fileCopyWorker;
                if(i != availableProcessors-1){
                    fileCopyWorker = new NIOFileTransferWorker(countDownLatch, file, dest, start, start + filePieceSize);
                } else {
                    fileCopyWorker = new NIOFileTransferWorker(countDownLatch, file, dest, start, start + filePieceSize + remainSize);
                }
                executorService.execute(fileCopyWorker);
                start+=filePieceSize;
            }
        } finally {
            countDownLatch.await();
        }
    }

}
