package com.cz.android.simplehttp.io.remote;

@FunctionalInterface
public interface OnComplete {

    void onComplete(FileWriterProxy fileWriter);
}
