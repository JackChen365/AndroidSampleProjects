package com.cz.android.text.layout.div;

import android.graphics.Paint;
import android.text.Spanned;
import android.text.TextPaint;

import com.cz.android.text.Styled;
import com.cz.android.text.layout.Layout;
import com.cz.android.text.style.MetricAffectingSpan;
import com.cz.android.text.utils.ArrayUtils;

/**
 * @author Created by cz
 * @date 2020/8/5 10:24 AM
 * @email bingo110@126.com
 */
public class DivisionStaticLayout extends DivisionLayout {
    private static final int BUFFER_SIZE =100;
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
    public DivisionStaticLayout(CharSequence source, TextPaint paint, int width) {
        super(source, paint, width, 0);
        columns = COLUMNS_NORMAL;
        outerWidth = width;
        fontMetricsInt = new Paint.FontMetricsInt();
        lines = new int[ArrayUtils.idealIntArraySize(2 * columns)];
    }

    private void generate(int width){

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
        Spanned spanned = null;
        if (source instanceof Spanned)
            spanned = (Spanned) source;
        int top = 0;
        int lineCount = getLineCount();
        if(0 < lineCount){
            top = getLineTop(lineCount);
        }
        float w = 0;
        int offset = here;
        boolean workComplete=false;
        int width = outerWidth;
        int fitAscent = 0, fitDescent = 0, fitTop = 0, fitBottom = 0;
        int okAscent = 0, okDescent = 0, okTop = 0, okBottom = 0;
        while(!workComplete && w <= outerWidth) {
            if (offset >= end) {
                start = offset;
                expandTextBuffer();
            }
            int next;
            if (spanned == null)
                next = end;
            else
                next = spanned.nextSpanTransition(offset, end, MetricAffectingSpan.class);

            if (spanned == null) {
                paint.getTextWidths(source, start, next, widths);
                paint.getFontMetricsInt(fm);
            } else if(null!=spanned){
                start = offset;
                workPaint.baselineShift = 0;
                Styled.getTextWidths(paint, workPaint, spanned, offset, next, widths, fm);
                if (workPaint.baselineShift < 0) {
                    fm.ascent += workPaint.baselineShift;
                    fm.top += workPaint.baselineShift;
                } else {
                    fm.descent += workPaint.baselineShift;
                    fm.bottom += workPaint.baselineShift;
                }
            }
            int fmTop = fm.top;
            int fmBottom = fm.bottom;
            int fmAscent = fm.ascent;
            int fmDescent = fm.descent;
            for (;offset < next; offset++) {
                char c = source.charAt(offset);


                if('\n' != c){
                    w += widths[offset - start];
                }
                if (w <= width) {
                    fit = offset + 1;
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
                                    (offset - 1 < here || !Character.isDigit(source.charAt(offset - 1))) &&
                                    (offset + 1 >= next || !Character.isDigit(source.charAt(offset + 1)))) ||
                            ((c == '/' || c == '-') && (offset + 1 >= next || !Character.isDigit(source.charAt(offset + 1 - start))))) {
                        ok = offset + 1;
                        if (fitTop < okTop)
                            okTop = fitTop;
                        if (fitAscent < okAscent)
                            okAscent = fitAscent;
                        if (fitDescent > okDescent)
                            okDescent = fitDescent;
                        if (fitBottom > okBottom)
                            okBottom = fitBottom;
                    }
                } else {
                    if (ok != here) {
                        top = out(here, ok, okAscent, okDescent, top);
                        here = ok;
                    } else if (fit != here) {
                        top = out(here, fit, fitAscent, fitDescent, top);
                        here = fit;
                    }
                    end = ok = here;
                    fitAscent = fitDescent = 0;
                    workComplete = true;
                    break;
                }
                if('\n' == c || offset ==source.length()-1){
                    out(here, fit, fitAscent, fitDescent, top);
                    end = here = ok = fit;
                    workComplete = true;
                    break;
                }
            }
        }

    }

    private void expandTextBuffer(){
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
