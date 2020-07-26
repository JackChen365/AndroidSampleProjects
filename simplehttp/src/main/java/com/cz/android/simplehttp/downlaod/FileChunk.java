package com.cz.android.simplehttp.downlaod;

import java.io.Serializable;

public class FileChunk implements Serializable {
    public final int index;
    public final long start;
    public final long end;
    /**
     * This progress indicates the download progress of this file chunk.
     */
    public long progress;
    /**
     * How many bytes this time increment.
     */
    public long delta;

    public FileChunk(int index, long start, long end) {
        this.index = index;
        this.start = start;
        this.progress = start;
        this.end = end;
    }


}
