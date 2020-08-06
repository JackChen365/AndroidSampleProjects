package com.cz.android.text.layout.buffer;

import android.graphics.Paint;
import android.text.TextPaint;

import com.cz.android.text.layout.Layout;
import com.cz.android.text.utils.ArrayUtils;
import com.cz.android.text.utils.TextUtilsCompat;

/**
 * @author Created by cz
 * @date 2020/8/5 10:24 AM
 * @email bingo110@126.com
 */
public class BufferedBoringStaticLayout extends Layout {
    private static final int BUFFER_SIZE =1;
    private static final int COLUMNS_NORMAL = 5;
    private static final int START = 0;
    private static final int TOP = 1;
    private static final int DESCENT = 2;
    private static final int START_MASK = 0x1FFFFFFF;

    private int outerWidth;
    private int start, end;
    private int here,ok,fit;

    private int lineCount;
    private int columns;

    private int[] lines;
    private float[] widths;


    private Paint.FontMetricsInt fontMetricsInt;
    /**
     * @param source      操作文本
     * @param paint      绘制paint
     * @param width      排版宽
     */
    public BufferedBoringStaticLayout(CharSequence source, TextPaint paint, int width) {
        super(source, paint, width, 0);
        columns = COLUMNS_NORMAL;
        outerWidth = width;
        fontMetricsInt = new Paint.FontMetricsInt();
        lines = new int[ArrayUtils.idealIntArraySize(2 * columns)];
    }

    public void outputLine(){
        outputLine(outerWidth);
    }

    public void outputLine(int outerWidth){
        CharSequence source = getText();
        if(null==source || here >= source.length()){
            return;
        }
        //The first time or we out of the buffer data.
        TextPaint paint = getPaint();
        Paint.FontMetricsInt fm = fontMetricsInt;
        int top = 0;
        int lineCount = getLineCount();
        if(0 < lineCount){
            top = getLineTop(lineCount);
        }
        int i = here;
        float w = 0;
        boolean workComplete=false;
        int width = outerWidth;
        int fitAscent = 0, fitDescent = 0, fitTop = 0, fitBottom = 0;
        while(!workComplete && w <= outerWidth) {
            if (i >= end) {
                start = i;
                readBuffer();
            }
            int next = end;
            float[] widths = this.widths;
            paint.getFontMetricsInt(fm);
            int fmTop = fm.top;
            int fmBottom = fm.bottom;
            int fmAscent = fm.ascent;
            int fmDescent = fm.descent;
            for (i = start; i < next; i++) {
                char c = source.charAt(i);
                if('\n' != c){
                    w += widths[i - start];
                }
                if (w <= width) {
                    fit = i + 1;
                    if (fmTop < fitTop)
                        fitTop = fmTop;
                    if (fmAscent < fitAscent)
                        fitAscent = fmAscent;
                    if (fmDescent > fitDescent)
                        fitDescent = fmDescent;
                    if (fmBottom > fitBottom)
                        fitBottom = fmBottom;
                    if (c == ' ' || c == '\t' ||
                            ((c == '.' || c == ',' || c == ':' || c == ';') &&
                                    (i - 1 < here || !Character.isDigit(source.charAt(i - 1))) &&
                                    (i + 1 >= next || !Character.isDigit(source.charAt(i + 1)))) ||
                            ((c == '/' || c == '-') && (i + 1 >= next || !Character.isDigit(source.charAt(i + 1 - start))))) {
                        ok = i + 1;
                    }
                } else {
                    if (ok != here) {
                        top = out(here, ok, fitAscent, fitDescent, top);
                        end = here = ok;
                    } else if (fit != here) {
                        top = out(here, fit, fitAscent, fitDescent, top);
                        end = here = fit;
                    }
                    fitAscent = fitDescent = 0;
                    workComplete = true;
                    break;
                }
                if('\n' == c || i ==source.length()-1){
                    out(here, fit, fitAscent, fitDescent, top);
                    end = here = fit;
                    workComplete = true;
                    break;
                }
            }
        }

    }

    private void readBuffer(){
        TextPaint paint = getPaint();
        CharSequence source = getText();
        end = start + BUFFER_SIZE;
        if(end > source.length()){
            end = source.length();
        }
        int bufferSize = end - start;
        if (widths == null) {
            this.widths = new float[ArrayUtils.idealIntArraySize((bufferSize + 1) * 2)];
        }
        if ((end - start) * 2 > widths.length) {
            widths = new float[ArrayUtils.idealIntArraySize((end - start) * 2)];
        }
        paint.getTextWidths(source, start, end, widths);
    }

    private int out( int start, int end, int above, int below,int v) {
        int j = lineCount;
        int off = j * columns;
        int want = off + columns + TOP;
        int[] lines = this.lines;

        if (want >= lines.length) {
            int nlen = ArrayUtils.idealIntArraySize(want + 1);
            int[] grow = new int[nlen];
            System.arraycopy(lines, 0, grow, 0, lines.length);
            this.lines = grow;
            lines = grow;
        }
        int extra=0;

        lines[off + START] = start;
        lines[off + TOP] = v;
        lines[off + DESCENT] = below + extra;

        v += (below - above) + extra;
        lines[off + columns + START] = end;
        lines[off + columns + TOP] = v;
        lineCount++;
        return v;
    }


    public int getLineCount() {
        return lineCount;
    }

    public int getLineTop(int line) {
        return lines[columns * line + TOP];
    }

    public int getLineDescent(int line) {
        return lines[columns * line + DESCENT];
    }

    public int getLineStart(int line) {
        return lines[columns * line + START] & START_MASK;
    }

}
