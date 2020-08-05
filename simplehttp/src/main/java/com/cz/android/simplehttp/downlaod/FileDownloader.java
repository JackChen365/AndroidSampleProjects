package com.cz.android.simplehttp.downlaod;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

public class FileDownloader implements Runnable {
    private static final Object LOCK=new Object();
    private final String downloadUrl;
    private final File destFile;
    private final MultiThreadingDownloader.Observable observable;
    private final CountDownLatch countDownLatch;
    private final FileChunk fileChunk;
    private final int bufferSize;

    public FileDownloader(String url, File destFile,MultiThreadingDownloader.Observable observable, CountDownLatch countDownLatch, FileChunk fileChunk, int bufferSize) {
        this.downloadUrl = url;
        this.destFile=destFile;
        this.observable=observable;
        this.countDownLatch=countDownLatch;
        this.fileChunk=fileChunk;
        this.bufferSize=bufferSize;
    }

    @Override
    public void run() {
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestProperty("Range", "bytes:"+fileChunk.progress+"-"+fileChunk.end);
            int responseCode = httpURLConnection.getResponseCode();
            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                System.out.println("Thread:"+Thread.currentThread().getName()+" start download file.");
                try(RandomAccessFile randomAccessFile=new RandomAccessFile(destFile,"rw")){
                    long read;
                    InputStream inputStream = httpURLConnection.getInputStream();
                    randomAccessFile.seek(fileChunk.progress);
                    byte[] byteBuffer=new byte[bufferSize];
                    while(-1!=(read=inputStream.read(byteBuffer))){
                        if(fileChunk.progress+read>fileChunk.end){
                            read = fileChunk.end-fileChunk.progress;
                        }
                        randomAccessFile.write(byteBuffer,0, (int) read);
                        long filePointer = randomAccessFile.getFilePointer();
                        synchronized (LOCK){
                            fileChunk.delta=read;
                            fileChunk.progress=filePointer;
                            observable.setChanged();
                            observable.notifyObservers(fileChunk);
                        }
                        if (filePointer >= fileChunk.end) {
                            //Over the boundary
                            break;
                        }
                    }
                    countDownLatch.countDown();
                    System.out.println("Thread:"+Thread.currentThread().getName()+" download completely!");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
