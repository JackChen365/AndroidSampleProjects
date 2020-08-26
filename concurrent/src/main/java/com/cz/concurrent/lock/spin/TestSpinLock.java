package com.cz.concurrent.lock.spin;

import java.util.concurrent.CountDownLatch;


public class TestSpinLock {
    
    final static int THREAD_NUM = 100;
    static int x = 0;

    public void testLock() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(THREAD_NUM);
        final SpinLock spinLock = new SpinLock();
        for (int i = 0; i < THREAD_NUM; i++) {
            // 启动子线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 每个线程循环多次，频繁上锁，解锁。
                    for (int n = 0; n < 100; n++) {
                        spinLock.lock();
                        x++;
                        spinLock.unLock();
                    }

                    latch.countDown();    // 子线程通知主线程，工作完毕。
                }
            }).start();
        }
        latch.await();    // 主线程等待所有子线程结束。
        System.out.println(x);    // 最终打印结果：10000 ，未出现线程不安全的异常。
    }
}