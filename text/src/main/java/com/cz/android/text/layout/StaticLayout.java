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
 * 自定义静态文本排版对象
 * 1. 支持TextView span设计
 * 2. 支持View以span形式存在
 * 3. 支持文本对元素的流式排版
 */
public class StaticLayout extends Layout {
    private static final int COLUMNS_NORMAL = 5;
    private static final int START = 0;
    private static final int TOP = 1;
    private static final int DESCENT = 2;
    private static final int LEFT = 3;//文本起始绘制位置
    private static final int ALIGN = 4;//文本对齐标志
    private static final int START_MASK = 0x1FFFFFFF;

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
     * @param spacingAdd 行额外添加空间
     */
    public StaticLayout(CharSequence source, TextPaint paint, int width, float spacingAdd) {
        super(source, paint, width, spacingAdd);

        columns = COLUMNS_NORMAL;
        fontMetricsInt = new Paint.FontMetricsInt();
        lines = new int[ArrayUtils.idealIntArraySize(2 * columns)];
        generate(source, 0,  source.length(), paint, width, spacingAdd);
        charArrays = null;
        widths = null;
        fontMetricsInt = null;
    }

    void generate(CharSequence source, int bufferStart, int bufferEnd,
                  TextPaint paint, int outerWidth,float spacingadd) {
        lineCount = 0;

        int v = 0;
        Paint.FontMetricsInt fm = fontMetricsInt;

        int end = TextUtilsCompat.indexOf(source, '\n', bufferStart, bufferEnd);
        int bufsiz = end >= 0 ? end - bufferStart : bufferEnd - bufferStart;
        boolean first = true;

        if (charArrays == null) {
            charArrays = new char[ArrayUtils.idealCharArraySize(bufsiz + 1)];
            widths = new float[ArrayUtils.idealIntArraySize((bufsiz + 1) * 2)];
        }
        char[] chs = charArrays;
        float[] widths = this.widths;

        Spanned spanned = null;
        if (source instanceof Spanned)
            spanned = (Spanned) source;
        for (int start = bufferStart; start <= bufferEnd; start = end) {
            if (first)
                first = false;
            else
                end = TextUtilsCompat.indexOf(source, '\n', start, bufferEnd);

            if (end < 0)
                end = bufferEnd;
            else
                end++;
            int firstWidth = outerWidth;

            if (end - start > chs.length) {
                chs = new char[ArrayUtils.idealCharArraySize(end - start)];
                charArrays = chs;
            }
            if ((end - start) * 2 > widths.length) {
                widths = new float[ArrayUtils.idealIntArraySize((end - start) * 2)];
                this.widths = widths;
            }

            TextUtilsCompat.getChars(source, start, end, chs, 0);

            if (source instanceof Spanned) {
                Spanned sp = (Spanned) source;
                ReplacementSpan[] spans = sp.getSpans(start, end, ReplacementSpan.class);

                for (int y = 0; y < spans.length; y++) {
                    int a = sp.getSpanStart(spans[y]);
                    int b = sp.getSpanEnd(spans[y]);

                    for (int x = a; x < b; x++) {
                        chs[x - start] = '\uFFFC';
                    }
                }
            }
            CharSequence sub = source;

            int width = firstWidth;

            float w = 0;
            int here = start;

            int ok = start;
            int okascent = 0, okdescent = 0, oktop = 0, okbottom = 0;

            int fit = start;
            int fitascent = 0, fitdescent = 0, fittop = 0, fitbottom = 0;
            int next;
            for (int i = start; i < end; i = next) {
                if (spanned == null)
                    next = end;
                else
                    next = spanned.nextSpanTransition(i, end, MetricAffectingSpan.class);
                if (spanned == null) {
                    paint.getTextWidths(sub, i, next, widths);
                    System.arraycopy(widths, 0, widths, end - start + (i - start), next - i);
                    paint.getFontMetricsInt(fm);
                } else {
                    workPaint.baselineShift = 0;
                    Styled.getTextWidths(paint, workPaint, spanned, i, next, widths, fm);
                    System.arraycopy(widths, 0, widths, end - start + (i - start), next - i);
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
                    char c = chs[j - start];

                    if (c == '\n') {
                        ;
                    } else {
                        w += widths[j - start + (end - start)];
                    }
                    if (w <= width) {
                        fit = j + 1;

                        if (fmTop < fittop)
                            fittop = fmTop;
                        if (fmAscent < fitascent)
                            fitascent = fmAscent;
                        if (fmDescent > fitdescent)
                            fitdescent = fmDescent;
                        if (fmBottom > fitbottom)
                            fitbottom = fmBottom;
                        if (c == ' ' || c == '\t' ||
                                ((c == '.' || c == ',' || c == ':' || c == ';') &&
                                        (j - 1 < here || !Character.isDigit(chs[j - 1 - start])) &&
                                        (j + 1 >= next || !Character.isDigit(chs[j + 1 - start]))) ||
                                ((c == '/' || c == '-') &&
                                        (j + 1 >= next || !Character.isDigit(chs[j + 1 - start])))) {
                            ok = j + 1;

                            if (fittop < oktop)
                                oktop = fittop;
                            if (fitascent < okascent)
                                okascent = fitascent;
                            if (fitdescent > okdescent)
                                okdescent = fitdescent;
                            if (fitbottom > okbottom)
                                okbottom = fitbottom;
                        }
                    } else {
                        if (ok != here) {
                            v = out(here, ok, okascent, okdescent,v);
                            here = ok;
                        } else if (fit != here) {
                            v = out(here, fit, fitascent, fitdescent, v);
                            here = fit;
                        }
                        j = here - 1;    // continue looping
                        ok = here;
                        w = 0;
                        fitascent = fitdescent = fittop = fitbottom = 0;
                        okascent = okdescent = oktop = okbottom = 0;
                    }
                }
            }
            //处理最后一行元素信息
            if (end != here) {
                v = out(here, end, fitascent, fitdescent, v);
            }
            if (end == bufferEnd)
                break;
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
