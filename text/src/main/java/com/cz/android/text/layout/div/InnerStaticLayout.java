package com.cz.android.text.layout.div;

import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Spanned;
import android.text.TextPaint;

import com.cz.android.text.Styled;
import com.cz.android.text.style.MetricAffectingSpan;
import com.cz.android.text.utils.ArrayUtils;

/**
 * @author Created by cz
 * @date 2020/8/5 10:24 AM
 * @email bingo110@126.com
 */
public class InnerStaticLayout extends DivisionLayout {
    private static final int COLUMNS_NORMAL = 5;
    private static final int START = 0;
    private static final int TOP = 1;
    private static final int DESCENT = 2;
    private static final int LEFT = 3;//文本起始绘制位置
    private static final int BOTTOM=4;//文本底部位置,因为存在一行内,多行信息
    private static final int START_MASK = 0x1FFFFFFF;
    private Paint.FontMetricsInt fitFontMetricsInt;
    private Paint.FontMetricsInt okFontMetricsInt;
    private Paint.FontMetricsInt fontMetricsInt;

    private int layoutOffset;
    private int outerWidth;

    private int lineCount;
    private int columns;
    private int[] lines;
    /**
     * @param source      操作文本
     * @param paint      绘制paint
     * @param width      排版宽
     */
    public InnerStaticLayout(CharSequence source, TextPaint paint,Paint.FontMetricsInt fm,Paint.FontMetricsInt okFm,Paint.FontMetricsInt fitFm, int width) {
        super(source, paint, width, 0);
        columns = COLUMNS_NORMAL;
        outerWidth = width;
        lines = new int[ArrayUtils.idealIntArraySize(2 * columns)];
        fontMetricsInt=fm;
        okFontMetricsInt=okFm;
        fitFontMetricsInt=fitFm;
    }

    public void outputLine(TextLayoutState layoutState,char c,float textWidth,int here){
        //The first time or we out of the buffer data.
        CharSequence source = getText();
        Paint.FontMetricsInt fm = fontMetricsInt;
        Paint.FontMetricsInt okFm = okFontMetricsInt;
        Paint.FontMetricsInt fitFm = fitFontMetricsInt;
        Spanned spanned = null;
        if (source instanceof Spanned)
            spanned = (Spanned) source;
        int top = 0;
        int lineCount = getLineCount();
        if(0 < lineCount){
            top = getLineTop(lineCount);
        }
        int next;
        if (spanned == null)
            next = layoutState.end;
        else
            next = spanned.nextSpanTransition(here, layoutState.end, MetricAffectingSpan.class);
        int fmTop = fm.top;
        int fmBottom = fm.bottom;
        int fmAscent = fm.ascent;
        int fmDescent = fm.descent;
        if('\n' != c){
            layoutOffset += textWidth;
        }
        if (layoutOffset <= outerWidth) {
            layoutState.fit = here + 1;
            if (fmTop < fitFm.top)
                fitFm.top = fmTop;
            if (fmAscent < fitFm.ascent)
                fitFm.ascent = fmAscent;
            if (fmDescent > fitFm.descent)
                fitFm.descent = fmDescent;
            if (fmBottom > fitFm.bottom)
                fitFm.bottom = fmBottom;
            if (c == ' ' || c == '\t' ||
                    ((c == '.' || c == ',' || c == ':' || c == ';') &&
                            (here - 1 < layoutState.here || !Character.isDigit(source.charAt(here - 1))) &&
                            (here + 1 >= next || !Character.isDigit(source.charAt(here + 1)))) ||
                    ((c == '/' || c == '-') && (here + 1 >= next || !Character.isDigit(source.charAt(here + 1 - layoutState.start))))) {
                layoutState.ok = here + 1;
                if (fitFm.top < okFm.top)
                    okFm.top = fitFm.top;
                if (fitFm.ascent < okFm.ascent)
                    okFm.ascent = fitFm.ascent;
                if (fitFm.descent > okFm.descent)
                    okFm.descent = fitFm.descent;
                if (fitFm.bottom > okFm.bottom)
                    okFm.bottom = fitFm.bottom;
            }
        } else {
            if (layoutState.ok != layoutState.here) {
                out(layoutState.here, layoutState.ok, okFm.ascent, okFm.descent,0, top);
                layoutState.here = layoutState.ok;
            } else if (layoutState.fit != layoutState.here) {
                out(layoutState.here, layoutState.fit, fitFm.ascent, fitFm.descent,0, top);
                layoutState.here = layoutState.fit;
            }
            layoutState.end = layoutState.ok = layoutState.here;
            okFm.top=okFm.ascent=okFm.descent=okFm.bottom=0;
            fitFm.top=fitFm.ascent=fitFm.descent=fitFm.bottom=0;
        }
        if('\n' == c || here ==source.length()-1){
            out(layoutState.here, layoutState.fit, fitFm.ascent, fitFm.descent,0, top);
            okFm.top=okFm.ascent=okFm.descent=okFm.bottom=0;
            fitFm.top=fitFm.ascent=fitFm.descent=fitFm.bottom=0;
            layoutState.end = layoutState.here = layoutState.ok = layoutState.fit;
        }
    }

    private void out(int start, int end, int above, int below, float x, float v) {
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
        //根据不同模式,确定位置
        lines[off + START] = start;
        lines[off + TOP] = (int)v;
        lines[off + DESCENT] = below;

        float lineHeight= (below - above);
        lines[off + LEFT] = (int) x;
        lines[off + BOTTOM] = (int) (v+lineHeight);

        lines[off + columns + START] = end;
        lines[off + columns + TOP] = (int)(v+lineHeight);
        lineCount++;
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
