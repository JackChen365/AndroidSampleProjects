package com.cz.android.text.layout.div;

import android.graphics.Canvas;
import android.graphics.Paint;
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
public class DivisionStaticLayout extends DivisionLayout {
    private static final int BUFFER_SIZE=100;
    private static final int COLUMNS_NORMAL = 5;
    private static final int START = 0;
    private static final int TOP = 1;
    private static final int DESCENT = 2;
    private static final int LEFT = 3;//文本起始绘制位置
    private static final int BOTTOM=4;//文本底部位置,因为存在一行内,多行信息
    private static final int START_MASK = 0x1FFFFFFF;
    private int outerWidth;

    private int lineCount;
    private int columns;

    private int[] lines;
    private float[] widths;

    private TextLayoutState layoutState;
    private InnerStaticLayout staticLayout;

    private Paint.FontMetricsInt fitFontMetricsInt;
    private Paint.FontMetricsInt okFontMetricsInt;
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
        layoutState=new TextLayoutState();
        fontMetricsInt = new Paint.FontMetricsInt();
        fitFontMetricsInt = new Paint.FontMetricsInt();
        okFontMetricsInt = new Paint.FontMetricsInt();
        lines = new int[ArrayUtils.idealIntArraySize(2 * columns)];
        staticLayout = new InnerStaticLayout(source,paint,fontMetricsInt,okFontMetricsInt,fitFontMetricsInt,width/2);
    }

    public boolean outputLine(){
        return outputLine(outerWidth);
    }

    public boolean outputLine(int outerWidth){
        CharSequence source = getText();
        if(null==source || layoutState.here >= source.length()){
            return false;
        }
        //The first time or we out of the buffer data.
        TextPaint paint = getPaint();
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
        float w = 0;
        int here = layoutState.here;
        while(w <= outerWidth) {
            if (here >= layoutState.end) {
                layoutState.start = here;
                expandTextBuffer();
            }
            int next;
            if (spanned == null)
                next = layoutState.end;
            else
                next = spanned.nextSpanTransition(here, layoutState.end, MetricAffectingSpan.class);

            if (spanned == null) {
                paint.getTextWidths(source, layoutState.start, next, widths);
                paint.getFontMetricsInt(fm);
            } else if(null!=spanned){
                layoutState.start = here;
                workPaint.baselineShift = 0;
                Styled.getTextWidths(paint, workPaint, spanned, here, next, widths, fm);
                if (workPaint.baselineShift < 0) {
                    fm.ascent += workPaint.baselineShift;
                    fm.top += workPaint.baselineShift;
                } else {
                    fm.descent += workPaint.baselineShift;
                    fm.bottom += workPaint.baselineShift;
                }
            }
            for (;here < next; here++) {
                char c = source.charAt(here);
                float textWidth = widths[here - layoutState.start];
                if(here > 20){
                    staticLayout.outputLine(layoutState,c,textWidth,here);
                } else {
                    if('\n' != c){
                        w += textWidth;
                    }
                    if(layoutTextInternal(source,top,here,next,c,w)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean layoutTextInternal(CharSequence source,int top,int here,int next,char c,float w){
        Paint.FontMetricsInt fm = fontMetricsInt;
        Paint.FontMetricsInt okFm = okFontMetricsInt;
        Paint.FontMetricsInt fitFm = fitFontMetricsInt;
        if (w <= outerWidth) {
            layoutState.fit = here + 1;
            if (fm.top < fitFm.top)
                fitFm.top = fm.top;
            if (fm.ascent < fitFm.ascent)
                fitFm.ascent = fm.ascent;
            if (fm.descent > fitFm.descent)
                fitFm.descent = fm.descent;
            if (fm.bottom > fitFm.bottom)
                fitFm.bottom = fm.bottom;
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
            return true;
        }
        if('\n' == c || here ==source.length()-1){
            out(layoutState.here, layoutState.fit, fitFm.ascent, fitFm.descent,0, top);
            okFm.top=okFm.ascent=okFm.descent=okFm.bottom=0;
            fitFm.top=fitFm.ascent=fitFm.descent=fitFm.bottom=0;
            layoutState.end = layoutState.here = layoutState.ok = layoutState.fit;
            return true;
        }
        return false;
    }

    private void expandTextBuffer(){
        CharSequence source = getText();
        layoutState.end = layoutState.start + BUFFER_SIZE;
        if(layoutState.end > source.length()){
            layoutState.end = source.length();
        }
        int bufferSize = layoutState.end - layoutState.start;
        if (widths == null) {
            this.widths = new float[ArrayUtils.idealIntArraySize((bufferSize + 1) * 2)];
        }
        if ((layoutState.end - layoutState.start) * 2 > widths.length) {
            widths = new float[ArrayUtils.idealIntArraySize((layoutState.end - layoutState.start) * 2)];
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

    @Override
    public int getHeight() {
        int height = super.getHeight();
        return height+staticLayout.getHeight();
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

    /**
     * Draw this Layout on the specified Canvas.
     */
    public void draw(Canvas c) {
        super.draw(c);
        c.save();
        int height = super.getHeight();
        c.translate(0,height);
        staticLayout.draw(c);
        c.restore();
    }

}
