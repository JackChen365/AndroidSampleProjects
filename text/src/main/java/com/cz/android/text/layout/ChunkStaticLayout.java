package com.cz.android.text.layout;

import android.graphics.Paint;
import android.text.Spanned;
import android.text.TextPaint;

import com.cz.android.text.Styled;
import com.cz.android.text.style.MetricAffectingSpan;
import com.cz.android.text.style.ReplacementSpan;
import com.cz.android.text.utils.ArrayUtils;
import com.cz.android.text.utils.TextUtilsCompat;

/**
 * @author Created by cz
 * @date 2020/8/5 10:24 AM
 * @email bingo110@126.com
 */
public class ChunkStaticLayout  extends Layout {
    private static final int COLUMNS_NORMAL = 5;
    private static final int START = 0;
    private static final int TOP = 1;
    private static final int DESCENT = 2;
    private static final int START_MASK = 0x1FFFFFFF;

    private int outerWidth;
    private int start,end;
    private int here,ok,fit;

    private int lineCount;
    private int columns;

    private int[] lines;
    private char[] charArrays;
    private float[] widths;


    private Paint.FontMetricsInt fontMetricsInt;
    /**
     * @param source      操作文本
     * @param paint      绘制paint
     * @param width      排版宽
     */
    public ChunkStaticLayout(CharSequence source, TextPaint paint, int width) {
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
        Spanned spanned = null;
        if (source instanceof Spanned)
            spanned = (Spanned) source;
        if(here >= end){
            start = ok = fit = here;
            end = TextUtilsCompat.indexOf(source, '\n', start, source.length());
            if(0 > end){
                end = source.length();
            } else {
                end ++;
            }
            int bufferSize = end - start;
            if (charArrays == null) {
                this.charArrays = new char[ArrayUtils.idealCharArraySize(bufferSize + 1)];
                this.widths = new float[ArrayUtils.idealIntArraySize((bufferSize + 1) * 2)];
            }
            if (end - start > charArrays.length) {
                charArrays = new char[ArrayUtils.idealCharArraySize(end - start)];
            }
            if ((end - start) * 2 > widths.length) {
                widths = new float[ArrayUtils.idealIntArraySize((end - start) * 2)];
            }
            if (source instanceof Spanned) {
                Spanned sp = (Spanned) source;
                ReplacementSpan[] spans = sp.getSpans(start, end, ReplacementSpan.class);

                for (int y = 0; y < spans.length; y++) {
                    int a = sp.getSpanStart(spans[y]);
                    int b = sp.getSpanEnd(spans[y]);
                    for (int x = a; x < b; x++) {
                        widths[x - start] = '\uFFFC';
                    }
                }
            }
            TextUtilsCompat.getChars(source, start, end, charArrays, 0);
        }
        char[] chs = charArrays;
        float[] widths = this.widths;

        int top = 0;
        int lineCount = getLineCount();
        if(0 < lineCount){
            top = getLineTop(lineCount);
        }
        int width = outerWidth;
        float w = 0;
        boolean workDone=false;
        int okAscent = 0, okDescent = 0, okTop = 0, okBottom = 0;
        int fitAscent = 0, fitDescent = 0, fitTop = 0, fitBottom = 0;
        int next;
        for (int i = here; i < end; i = next) {
            if (spanned == null)
                next = end;
            else
                next = spanned.nextSpanTransition(i, end, MetricAffectingSpan.class);

            if (spanned == null) {
                paint.getTextWidths(source, start, next, widths);
                paint.getFontMetricsInt(fm);
            } else if(null!=spanned){
                workPaint.baselineShift = 0;
                Styled.getTextWidths(paint, workPaint, spanned, here, next, widths, fm);
                System.arraycopy(widths, 0, widths, i, next-i);
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
            for (int j = i; j < next; j++) {
                char c = chs[j-start];
                if(c != '\n'){
                    w += widths[j-start];
                }
                if (w <= width) {
                    fit = j + 1;

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
                                    (j - 1 < here || !Character.isDigit(chs[j - 1 - start])) &&
                                    (j + 1 >= next || !Character.isDigit(chs[j + 1 - start]))) ||
                            ((c == '/' || c == '-') &&
                                    (j + 1 >= next || !Character.isDigit(chs[j + 1 - start])))) {
                        ok = j + 1;
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
                        top = out(here, ok, okAscent, okDescent,top);
                        here = ok;
                    } else if (fit != here) {
                        top = out(here, fit, fitAscent, fitDescent, top);
                        here = fit;
                    }
                    fit = ok = here;
                    workDone=true;
                    break;
                }
            }
            if(workDone){
                break;
            }
        }
        if (!workDone && here != end) {
            out(here, end, fitAscent, fitDescent, top);
            here = fit;
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
