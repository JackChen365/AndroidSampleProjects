package com.cz.android.simplehttp.io.remote;


import com.cz.android.simplehttp.io.Constants;
import com.cz.android.simplehttp.io.StringUtils;

final class FileMetaData {

    private final String fileName;
    private final long size;

    static FileMetaData from(final String request) {
        assert StringUtils.isNotEmpty(request);

        final String[] contents = request.replace(Constants.END_MESSAGE_MARKER, "").split(Constants.MESSAGE_DELIMITTER);
        return new FileMetaData(contents[0], Long.valueOf(contents[1]));
    }

    private FileMetaData(final String fileName, final long size) {
        assert StringUtils.isNotEmpty(fileName);

        this.fileName = fileName;
        this.size = size;
    }

    String getFileName() {
        return this.fileName;
    }

    long getSize() {
        return this.size;
    }
}
