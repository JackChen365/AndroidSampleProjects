package com.cz.android.simplehttp.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerAndConsumer {
    private static final int MAX_SIZE=1000;
    public static void main(String[] args) {
        final Lock lock=new ReentrantLock();
        final Condition produceCondition = lock.newCondition();
        final Condition consumeCondition = lock.newCondition();
        final BlockingQueue<String> baskets=new LinkedBlockingQueue<>();

        //All the operation about producer.
        final int producerSize=7;
        final AtomicInteger produceCounter=new AtomicInteger();
        ExecutorService producerExecutorService = Executors.newFixedThreadPool(producerSize);
        List<Producer> producerList=new ArrayList<>(producerSize);
        for(int i=0;i<producerSize;i++){
            producerList.add(new Producer(lock,produceCondition,consumeCondition, baskets,produceCounter));
        }
        Executors.newSingleThreadExecutor().execute(()->{
            for(int i=0;i<producerSize;i++){
                Producer producer = producerList.get(i);
                producerExecutorService.execute(producer);
            }
        });

        //All the operation about consumer.
        final int consumerSize=3;
        final AtomicInteger consumeCounter=new AtomicInteger();
        ExecutorService consumerExecutorService = Executors.newFixedThreadPool(consumerSize);
        List<Consumer> consumerList=new ArrayList<>(producerSize);
        for(int i=0;i<consumerSize;i++){
            consumerList.add(new Consumer(lock,produceCondition,consumeCondition, baskets,consumeCounter));
        }

        Executors.newSingleThreadExecutor().execute(()->{
            for(int i=0;i<consumerSize;i++){
                Consumer consumer = consumerList.get(i);
                consumerExecutorService.execute(consumer);
            }
        });
    }

    static class Producer implements Runnable{
        private final Lock lock;
        private final Condition produceCondition;
        private final Condition consumeCondition;
        private final BlockingQueue<String> baskets;
        private final AtomicInteger produceCounter;

        public Producer(Lock lock,Condition produceCondition,Condition consumeCondition, BlockingQueue<String> baskets,AtomicInteger counter) {
            this.lock=lock;
            this.produceCondition = produceCondition;
            this.consumeCondition = consumeCondition;
            this.baskets = baskets;
            this.produceCounter=counter;
        }

        @Override
        public void run() {
            while (true){
                try {
                    lock.lock();
                    int count = produceCounter.incrementAndGet();
                    String message="Product:"+count;
                    baskets.offer(message);
                    System.out.println("\t\t"+Thread.currentThread().getName()+" "+message);
                    consumeCondition.signal();
                    produceCondition.await();
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    static class Consumer implements Runnable{
        private final Lock lock;
        private final Condition produceCondition;
        private final Condition consumeCondition;
        private final BlockingQueue<String> baskets;
        private final AtomicInteger consumeCounter;

        public Consumer(Lock lock,Condition produceCondition,Condition consumeCondition, BlockingQueue<String> baskets,AtomicInteger counter) {
            this.lock=lock;
            this.produceCondition = produceCondition;
            this.consumeCondition = consumeCondition;
            this.baskets = baskets;
            this.consumeCounter =counter;
        }
        @Override
        public void run() {
            while (true){
                try {
                    lock.lock();
                    int count = consumeCounter.incrementAndGet();
                    String message = baskets.take();
                    System.out.println(Thread.currentThread().getName()+" The No."+count+":"+message);
                    produceCondition.signal();
                    consumeCondition.await();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }


}
