package com.cz.concurrent.lock.spin;

import java.util.concurrent.atomic.AtomicReference;

public class SpinLock {
    private AtomicReference<Thread> owner = new AtomicReference<>(); //不调用有参构造函数，则实例为null
    
    public void lock(){
        Thread currentThread = Thread.currentThread();
        while(!owner.compareAndSet(null, currentThread)){  // owner == null ，则compareAndSet返回true，否则为false。
            //拿不到owner的线程，不断的在死循环
        }
    }
    
    public void unLock(){
        owner.set(null);
        // 也可以这样写，太麻烦，没必要
        /*
        Thread cur = Thread.currentThread();  
        owner.compareAndSet(cur, null);
         */
    }
    
}