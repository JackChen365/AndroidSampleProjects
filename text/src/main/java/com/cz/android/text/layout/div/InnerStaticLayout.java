package com.cz.android.text.layout.div;

import android.graphics.Canvas;
import android.graphics.Color;
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
    /**
     * 跟随内容
     */
    private static final int FLOW_FLAG=0x01;
    /**
     * 断行标志
     */
    private static final int BREAK_LINE_FLAG = 0x02;
    /**
     * 检测是否需要断行,意思为超出内容则断,不超出不断
     */
    private static final int CONSIDER_BREAK_LINE_FLAG = 0x04;
    /**
     * 当前元素独占一行
     */
    private static final int SINGLE_LINE_FLAG = 0x08;

    /**
     * 遇到此控件自动换行
     */
    public static final int BREAK_LINE=BREAK_LINE_FLAG;
    /**
     * 标志span按flow摆放,但如果超出尺寸 ,则放到下一行
     */
    public static final int CONSIDER_BREAK_LINE=FLOW_FLAG | CONSIDER_BREAK_LINE_FLAG;
    /**
     * 独占一行
     */
    public static final int SINGLE_LINE=BREAK_LINE_FLAG | SINGLE_LINE_FLAG;
    /**
     * 跟随内容向右流动
     */
    public static final int FLOW=FLOW_FLAG;

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

    public boolean outputLine(TextLayoutState layoutState,char c,float textWidth,int here,int next){
        //The first time or we out of the buffer data.
        CharSequence source = getText();
        Paint.FontMetricsInt fm = fontMetricsInt;
        Paint.FontMetricsInt okFm = okFontMetricsInt;
        Paint.FontMetricsInt fitFm = fitFontMetricsInt;
        if('\n' != c){
            layoutOffset += textWidth;
        }
        if (layoutOffset <= outerWidth) {
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
            int layoutTop = getLayoutTop();
            if (layoutState.ok != layoutState.here) {
                out(layoutState.here, layoutState.ok, okFm.ascent, okFm.descent,0, layoutTop);
                layoutState.here = layoutState.ok;
            } else if (layoutState.fit != layoutState.here) {
                out(layoutState.here, layoutState.fit, fitFm.ascent, fitFm.descent,0, layoutTop);
                layoutState.here = layoutState.fit;
            }
            layoutOffset = 0;
            layoutState.end = layoutState.ok = layoutState.here;
            okFm.top=okFm.ascent=okFm.descent=okFm.bottom=0;
            fitFm.top=fitFm.ascent=fitFm.descent=fitFm.bottom=0;
            return true;
        }
        if('\n' == c || here ==source.length()-1){
            int layoutTop = getLayoutTop();
            out(layoutState.here, layoutState.fit, fitFm.ascent, fitFm.descent,0, layoutTop);
            layoutOffset = 0;
            okFm.top=okFm.ascent=okFm.descent=okFm.bottom=0;
            fitFm.top=fitFm.ascent=fitFm.descent=fitFm.bottom=0;
            layoutState.end = layoutState.here = layoutState.ok = layoutState.fit;
            return true;
        }
        return false;
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

    /**
     * If this layout is in flow mode. this mean it won't break the line and if the content's size out the screen. It will still in this line.
     */
    public static boolean isFlow(int layoutMode){
        return 0!=(FLOW_FLAG & layoutMode);
    }

    /**
     * Check if the layout should break the line. if the this layout shows up.
     */
    public static boolean isBreakLine(int layoutMode){
        return 0!=(BREAK_LINE_FLAG & layoutMode);
    }

    /**
     * Check if the layout is in single line mode.
     */
    public static boolean isSingleLine(int layoutMode){
        return 0!=(SINGLE_LINE_FLAG & layoutMode);
    }

    /**
     * If the content's side out of the screen. It will break the line automatically.
     */
    public static boolean considerBreakLine(int layoutMode){
        return 0!=(CONSIDER_BREAK_LINE_FLAG & layoutMode);
    }

    /**
     * 获得元素排版模式
     * @return
     */
    public int getSpanLayoutMode(){
        return FLOW;
    }

    private int getLayoutTop(){
        int layoutTop = 0;
        int lineCount = getLineCount();
        if(0 < lineCount){
            layoutTop = getLineTop(lineCount);
        }
        return layoutTop;
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

    @Override
    public void draw(Canvas c) {
        super.draw(c);
        debugDraw(c);
    }


    /**
     * 调试绘制信息
     */
    private void debugDraw(Canvas canvas) {
        Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(1f);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        //绘制内边距
        int width = getWidth();
        int height = getHeight();
        canvas.drawRect(new Rect(0,0,width,height),paint);
        //绘制每一行间隔
        int lineCount = getLineCount();
        canvas.save();
        for(int i=0;i<lineCount;i++){
            int lineBottom = getLineBottom(i);
            canvas.drawLine(0f,lineBottom*1f, width*1f,lineBottom*1f,paint);
        }
        canvas.restore();
    }
}
