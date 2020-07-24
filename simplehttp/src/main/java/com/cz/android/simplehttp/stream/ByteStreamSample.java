package com.cz.android.simplehttp.stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author Created by cz
 * @date 2020/7/21 11:00 AM
 * @email bingo110@126.com
 */
public class ByteStreamSample {
    public static void main(String[] args) throws FileNotFoundException {
        ByteBuffer byteBuffer=ByteBuffer.allocate(1*1024);
        FileOutputStream outputStream=new FileOutputStream(new File(""));
    }
}
