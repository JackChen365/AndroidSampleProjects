package com.cz.android.simplehttp.downlaod;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class MultiThreadingDownloader {
    private static final String TMP_SUFFIX="tmp";
    public static void main(String[] args) throws IOException, InterruptedException {
        String url = "http://localhost:8090/"+
                URLEncoder.encode("resources/The Hobbit The Desolation of Smaug.mp4",StandardCharsets.UTF_8.toString());
        download(url);
    }

    private static void download(String downloadUrl) throws IOException, InterruptedException {
        URL url = new URL(downloadUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("HEAD");
        long contentLength = httpURLConnection.getContentLengthLong();
        int responseCode = httpURLConnection.getResponseCode();
        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String disposition = httpURLConnection.getHeaderField("Content-Disposition");
            String fileName=null;
            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                String decodeUrl = URLDecoder.decode(downloadUrl, StandardCharsets.UTF_8.toString());
                fileName = decodeUrl.substring(decodeUrl.lastIndexOf("/") + 1);
            }
            File destFile=new File("download/"+fileName+"."+TMP_SUFFIX);
            httpURLConnection.disconnect();

            File processFile=new File("download/cache");
            if(!processFile.exists()){
                processFile.mkdir();
            }
            File cacheProcessFile = new File(processFile, String.valueOf(downloadUrl.hashCode()));
            if(!cacheProcessFile.exists()){
                //Download file with any file cache.
                downloadFile(downloadUrl,destFile,cacheProcessFile, contentLength);
            } else {
                //Interrupt downloading before, Now continue to downloading.
                downloadFileFromCache(downloadUrl,destFile,cacheProcessFile,contentLength);
            }
        }
    }

    private static void downloadFile(String downloadUrl, File destFile, final File cacheProcessFile, final long contentLength) throws InterruptedException {
        int availableProcessors = Runtime.getRuntime().availableProcessors() + 1;
        //Split the file into pieces.
        long remainSize = 0;
        long filePieceSize = contentLength / availableProcessors;
        //If we still have remain size after splitting.
        if(filePieceSize*availableProcessors!=contentLength){
            remainSize = contentLength - (filePieceSize * availableProcessors);
        }
        final int bufferSize=1*1024*1024;
        final List<FileChunk> fileChunkList=new ArrayList<>();
        final Observable observable=new Observable();
        final AtomicLong downloadFileSize=new AtomicLong();
        observable.addObserver(new Observer() {
            @Override
            public void update(java.util.Observable observable, Object arg) {
                if(arg instanceof FileChunk){
                    FileChunk fileChunk = (FileChunk) arg;
                    long totalDownloadFile = downloadFileSize.addAndGet(fileChunk.delta);
                    float progress = (totalDownloadFile * 1f / contentLength * 100);
                    System.out.println("Thread:"+Thread.currentThread().getName()+" Download progress:"+String.format("%.2f", progress)+"%");
                }
                //Save the download chunk information to file.
                try {
                    if(!cacheProcessFile.exists()){
                        cacheProcessFile.createNewFile();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try(ObjectOutputStream objectOutputStream=new ObjectOutputStream(new FileOutputStream(cacheProcessFile))){
                    objectOutputStream.writeObject(fileChunkList);
                    objectOutputStream.flush();
                } catch (IOException e) {
                    System.err.println("Update the download process failed!");
                }
            }
        });
        CountDownLatch countDownLatch=new CountDownLatch(availableProcessors);
        try {
            int start=0;
            System.out.println("Process file length:"+contentLength);
            for(int i=0;i<availableProcessors;i++){
                FileChunk fileChunk;
                if(i != availableProcessors-1){
                    fileChunk = new FileChunk(i, start, start + filePieceSize);
                } else {
                    fileChunk = new FileChunk(i, start, start + filePieceSize + remainSize);
                }
                fileChunkList.add(fileChunk);
                FileDownloader fileDownloader = new FileDownloader(downloadUrl,destFile,observable,countDownLatch,fileChunk,bufferSize);
                System.out.println("Index:"+(i+1)+" start:"+fileChunk.start+" end:"+fileChunk.end);
                new Thread(fileDownloader).start();
                start+=filePieceSize;
            }
        } finally {
            countDownLatch.await();
            cacheProcessFile.deleteOnExit();
            String name = destFile.getName();
            File parentFile = destFile.getParentFile();
            File originalFile = new File(parentFile, name.substring(0, name.length()-TMP_SUFFIX.length() - 1));
            destFile.renameTo(originalFile);
            System.out.println("All task finished.");
        }
    }

    private static void downloadFileFromCache(String downloadUrl, File destFile, final File cacheProcessFile, final long contentLength) throws InterruptedException {
        final List<FileChunk> fileChunkList=new ArrayList<>();
        try(ObjectInputStream objectInputStream=new ObjectInputStream(new FileInputStream(cacheProcessFile))){
            List<FileChunk> cachedFileChunkList= (List<FileChunk>) objectInputStream.readObject();
            if(null!=cachedFileChunkList){
                fileChunkList.addAll(cachedFileChunkList);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if(fileChunkList.isEmpty()){
            System.err.println("We found nothing about the file.");
            cacheProcessFile.delete();
            downloadFile(downloadUrl,destFile,cacheProcessFile,contentLength);
        } else {
            final int bufferSize=1*1024*1024;
            final Observable observable=new Observable();
            final AtomicLong downloadFileSize=new AtomicLong();
            fileChunkList.forEach(new Consumer<FileChunk>() {
                @Override
                public void accept(FileChunk fileChunk) {
                    long delta = fileChunk.progress-fileChunk.start;
                    downloadFileSize.addAndGet(delta);
                }
            });
            observable.addObserver(new Observer() {
                @Override
                public void update(java.util.Observable observable, Object arg) {
                    if(arg instanceof FileChunk){
                        FileChunk fileChunk = (FileChunk) arg;
                        long totalDownloadFile = downloadFileSize.addAndGet(fileChunk.delta);
                        float progress = (totalDownloadFile * 1f / contentLength * 100);
                        System.out.println("Thread:"+Thread.currentThread().getName()+" Download progress:"+String.format("%.2f", progress)+"%");
                    }
                    //Save the download chunk information to file.
                    try {
                        if(!cacheProcessFile.exists()){
                            cacheProcessFile.createNewFile();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try(ObjectOutputStream objectOutputStream=new ObjectOutputStream(new FileOutputStream(cacheProcessFile))){
                        objectOutputStream.writeObject(fileChunkList);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            CountDownLatch countDownLatch=new CountDownLatch(fileChunkList.size());
            try {
                System.out.println("Process file length:"+contentLength);
                for(int i=0;i<fileChunkList.size();i++){
                    FileChunk fileChunk = fileChunkList.get(i);
                    if(fileChunk.progress==fileChunk.end){
                        countDownLatch.countDown();
                        System.out.println("The chunk part already download completed!");
                    } else {
                        FileDownloader fileDownloader = new FileDownloader(downloadUrl,destFile,observable,countDownLatch,fileChunk,bufferSize);
                        System.out.println("Index:"+(i+1)+" start:"+fileChunk.start+" end:"+fileChunk.end);
                        new Thread(fileDownloader).start();
                    }
                }
            } finally {
                countDownLatch.await();
                cacheProcessFile.deleteOnExit();
                String name = destFile.getName();
                File parentFile = destFile.getParentFile();
                File originalFile = new File(parentFile, name.substring(0, name.length()-TMP_SUFFIX.length() - 1));
                destFile.renameTo(originalFile);
                System.out.println("All task finished.");
            }
        }
    }

    public static class Observable extends java.util.Observable {
        public Observable(){
        }

        @Override
        public synchronized void setChanged() {
            super.setChanged();
        }
    }
}
