package com.cz.android.text.layout.div.chunk;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Spanned;
import android.text.TextPaint;

import com.cz.android.text.Styled;
import com.cz.android.text.layout.div.TextLayout;
import com.cz.android.text.layout.div.TextLayoutState;

/**
 * 文本排版layout
 * 当前实现功能
 * 1. 实现多行文本排版
 * 2. 实现断行策略抽离
 * 3. 实现span
 */
public abstract class TextChunk {
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
    private static Rect tempRect = new Rect();

    Paint.FontMetricsInt fitFontMetricsInt;
    Paint.FontMetricsInt okFontMetricsInt;
    Paint.FontMetricsInt fontMetricsInt;
    TextPaint workPaint;

    private CharSequence text;
    private TextAnchor textAnchor;
    private TextPaint paint;
    private int width;
    private float spacingAdd;
    private Spanned spanned;
    private boolean spannedText;

    /**
     *
     * @param width 排版宽
     * @param spacingAdd  行额外添加空间
     */
    protected TextChunk(int width, float spacingAdd,int line,int lineOffset,int offset) {
        if (width < 0)
            throw new IllegalArgumentException("Layout: " + width + " < 0");
        this.textAnchor=new TextAnchor(line,lineOffset,offset);
        this.workPaint = new TextPaint();
        this.width = width;
        this.spacingAdd = spacingAdd;
        if(text instanceof Spanned)
            spanned = (Spanned) text;
        spannedText = text instanceof Spanned;
    }

    public final void attachToTextLayout(TextLayout layout){
        this.paint = layout.getPaint();
        this.text = layout.getText();
        this.fontMetricsInt =layout.getFontMetricsInt();
        this.fitFontMetricsInt =layout.getFitFontMetricsInt();
        this.okFontMetricsInt =layout.getOkFontMetricsInt();
        onAttachToLayout(layout);
    }

    protected void onAttachToLayout(TextLayout layout){
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

    public Paint.FontMetricsInt getFitFontMetricsInt() {
        return fitFontMetricsInt;
    }

    public Paint.FontMetricsInt getOkFontMetricsInt() {
        return okFontMetricsInt;
    }

    public Paint.FontMetricsInt getFontMetricsInt() {
        return fontMetricsInt;
    }

    public TextAnchor getTextAnchor() {
        return textAnchor;
    }

    public boolean applyForPosition(int line,int lineOffset){
        return false;
    }

    public boolean applyForPosition(int offset){
        return false;
    }

    public abstract boolean onTextLayout(TextLayoutState layoutState,float w, char c, int here, int next);

    /**
     * 获得指定类型的span对象
     * @param start 查找文本起始位置
     * @param end 查找文本结束位置
     * @param type 查找对象字节码
     * @param <T>
     * @return 查找元素数组
     */
    public <T> T[] getSpans(int start, int end, Class<T> type) {
        return spanned.getSpans(start, end, type);
    }

    /**
     * 返回行附加空间
     * @return
     */
    public final float getSpacingAdd() {
        return spacingAdd;
    }

    /**
     * 返回行个数
     * @return
     */
    public abstract int getLineCount();

    /**
     * 返回文本
     * @return
     */
    public final CharSequence getText() {
        return text;
    }

    /**
     * 返回操作Paint对象,也是关联操作View的Paint对象
     * @return
     */
    public final TextPaint getPaint() {
        return paint;
    }

    /**
     * 当前操作尺寸宽度
     * @return
     */
    public final int getWidth() {
        return width;
    }

    /**
     * 返回当前所占高度
     * @return
     */
    public int getHeight() {
        return getLineTop(getLineCount());
    }

    /**
     * 返回指定行起始高
     * @param line
     * @return
     */
    public abstract int getLineTop(int line);

    /**
     * 获得行结束高度,此处暂不存在行间距运算
     * @param line
     * @return
     */
    public final int getLineBottom(int line) {
        return getLineTop(line + 1);
    }

    /**
     * 返回指定行文本Descent位置
     * @param line
     * @return
     */
    public abstract int getLineDescent(int line);

    /**
     * 返回指定行起始字符
     * @param line
     * @return
     */
    public abstract int getLineStart(int line);

    /**
     * 返回行结束字符
     * @param line
     * @return
     */
    public final int getLineEnd(int line) {
        return getLineStart(line + 1);
    }

    /**
     * 返回指定y轨位置所在行
     * @param vertical 纵轨位置
     * @return
     */
    public int getLineForVertical(int vertical) {
        int high = getLineCount(), low = -1, guess;

        while (high - low > 1) {
            guess = (high + low) / 2;

            if (getLineTop(guess) > vertical)
                high = guess;
            else
                low = guess;
        }

        if (low < 0)
            return 0;
        else
            return low;
    }

    /**
     * 返回可见的行位置
     * @param line
     * @param start
     * @param end
     * @return
     */
    private int getLineVisibleEnd(int line, int start, int end) {
        CharSequence text = this.text;
        char ch;
        if (line == getLineCount() - 1) {
            return end;
        }

        for (; end > start; end--) {
            ch = text.charAt(end - 1);

            if (ch == '\n') {
                return end - 1;
            }

            if (ch != ' ' && ch != '\t') {
                break;
            }

        }

        return end;
    }

    public int getSpanStart(Object tag) {
        return spanned.getSpanStart(tag);
    }

    public int getSpanEnd(Object tag) {
        return spanned.getSpanEnd(tag);
    }

    public int getSpanFlags(Object tag) {
        return spanned.getSpanFlags(tag);
    }

    public int nextSpanTransition(int start, int limit, Class type) {
        return spanned.nextSpanTransition(start, limit, type);
    }

    /**
     * Draw this Layout on the specified Canvas.
     */
    public void draw(Canvas c) {
        int dtop, dbottom;
        synchronized (tempRect) {
            if (!c.getClipBounds(tempRect)) {
                return;
            }
            dtop = tempRect.top;
            dbottom = tempRect.bottom;
        }
        int top = 0;
        int bottom = getLineTop(getLineCount());

        if (dtop > top) {
            top = dtop;
        }
        if (dbottom < bottom) {
            bottom = dbottom;
        }
        int first = getLineForVertical(top);
        int last = getLineForVertical(bottom);
        int previousLineBottom = getLineTop(first);
        int previousLineEnd = getLineStart(first);

        TextPaint paint = this.paint;
        CharSequence buf = text;
        boolean spannedText = this.spannedText;
        for (int i = first; i <= last; i++) {
            int start = previousLineEnd;
            previousLineEnd = getLineStart(i+1);
            int end = getLineVisibleEnd(i, start, previousLineEnd);
            int ltop = previousLineBottom;
            int lbottom = getLineTop(i+1);
            previousLineBottom = lbottom;
            int lbaseline = lbottom - getLineDescent(i);

            int left = 0;
            int x= left;
            if (!spannedText) {
                c.drawText(buf, start, end, x, lbaseline, paint);
            } else {
                Styled.drawText(c, buf, start, end, x, ltop, lbaseline, lbottom, paint, workPaint, false);
            }
        }
    }
}
