package com.cz.android.datastructure;

import com.cz.android.datastructure.arraymap.ArrayMap;

import org.junit.Test;

import java.util.HashMap;

/**
 * @author Created by cz
 * @date 2020/7/6 4:02 PM
 * @email bingo110@126.com
 */
public class ArrayMapTester {

    @Test
    public void hashMapTest(){
        long st = System.currentTimeMillis();
        HashMap<Integer,Integer> hashMap=new HashMap<>();
        for(int i=1000000;i>=0;i--){
            hashMap.put(i,i);
        }
        System.out.println("time1:"+(System.currentTimeMillis()-st));

        st=System.currentTimeMillis();
        ArrayMap arrayMap=new ArrayMap<Integer,Integer>();
        for(int i=1000000;i>=0;i--){
            arrayMap.put(i,i);
        }
        System.out.println("time2:"+(System.currentTimeMillis()-st));
//        time1:131
//        time2:64
    }

}
