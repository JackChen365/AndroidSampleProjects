package com.cz.android.text.layout.div.chunk;

public class TextAnchor {
    private final int line;
    private final int lineOffset;
    private final int offset;

    public TextAnchor(int line, int lineOffset) {
        this(line,lineOffset,-1);
    }

    public TextAnchor(int offset) {
        this(-1,-1,offset);
    }

    public TextAnchor(int line, int lineOffset, int offset) {
        this.line = line;
        this.lineOffset = lineOffset;
        this.offset = offset;
    }
}
