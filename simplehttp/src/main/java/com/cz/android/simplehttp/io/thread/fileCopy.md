## File copy test

> We use four different ways to copy the file from one place to another. First test the preference use single thread, And then test the preference use multi-thread.

Here is the test file and my computer information

```
File:Doom Annihilation.mkv
Size:1,839,867,032 bytes (1.84 GB on disk)


Model Name: MacBook Pro
Model Identifier: MacBookPro14,3
Processor Name: Quad-Core Intel Core i7
Processor Speed: 2.9 GHz
Number of Processors: 1
Total Number of Cores: 4
L2 Cache (per Core): 256 KB
L3 Cache: 8 MB
Hyper-Threading Technology: Enabled
Memory: 16 GB
```



The single thread tester could be really easy to understand.

```
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
``` 

As you can see above. We use the most simply ordinary stream to copy the file.

The second one we use NIO to transfer the file

```
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
```

Also It is very simple. Be aware of the class FileChannel. We can use the api: write/read as well.
But without use an extra ByteBuffer, This code snippet looks really nice.

Finally we start copy file with multi-thread.

The first one is the ordinary stream class called: RandomAccessFile. However it is the only class we could write or read byte from wherever we want.
This is really important for multi-thread copy.

The code is really easy to understand. First we split the file into different range. It is actually depends on your available processor.

```
long fileLength = file.length();
int availableProcessors = Runtime.getRuntime().availableProcessors() + 1;
//Split the file into pieces.
long remainSize = 0;
long filePieceSize = fileLength / availableProcessors;
//If we still have remain size after splitting.
if(filePieceSize*availableProcessors!=fileLength){
    remainSize = fileLength - (filePieceSize * availableProcessors);
}
```

And then we start copy different chunk data from the original file.

```
int start=0;
//We use countdown latch to block the main thread and after all the tasks finished. The latch opened.
CountDownLatch countDownLatch=new CountDownLatch(availableProcessors);
try {
    ThreadPoolExecutor executorService = new ThreadPoolExecutor(availableProcessors, availableProcessors, 1L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
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
```

The code snippet shows how the task works.

```
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
```


The final one is actually same like the thire one.

```
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
```


Note, We test all the samples in the same buffer size either in single thread or multi-thread.

And this is the test code.

```
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

Here are the message the console output:

The first method cost time:2226
The second method cost time:1608
The third method cost time:1753
The fourth method cost time:592


Again
The first method cost time:2310
The second method cost time:1645
The third method cost time:1629
The fourth method cost time:577

And again
The first method cost time:2293
The second method cost time:1632
The third method cost time:1460
The fourth method cost time:483
```

Let's make a conclusion for this test. The answer is more clearly for us.
Use multi-thread with file channel is more faster than just use multi-thread with the ordinary stream API
What's more, Use the file channel to transfer the file almost as fast as multi-thread with the ordinary stream.
This mean you should use file channel instead of use ordinary stream.
I thought use multi-thread will be faster then use single-thread. It turns out the RandomAccessFile is slow than I expected.

That is it. Good luck for you.