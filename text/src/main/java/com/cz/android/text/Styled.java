package com.cz.android.text;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Spanned;
import android.text.TextPaint;

import com.cz.android.text.style.CharacterStyle;
import com.cz.android.text.style.MetricAffectingSpan;
import com.cz.android.text.style.ReplacementSpan;

/**
 * This class provides static methods for drawing and measuring styled text,
 * like {@link Spanned} object with
 * {@link ReplacementSpan}.
 *
 */
public class Styled
{
    /**
     * Draws and/or measures a uniform run of text on a single line. No span of
     * interest should start or end in the middle of this run (if not
     * drawing, character spans that don't affect metrics can be ignored).
     * Neither should the run direction change in the middle of the run.
     *
     * <p>The x position is the leading edge of the text. In a right-to-left
     * paragraph, this will be to the right of the text to be drawn. Paint
     * should not have an Align value other than LEFT or positioning will get
     * confused.
     *
     * <p>On return, workPaint will reflect the original paint plus any
     * modifications made by character styles on the run.
     *
     * <p>The returned width is signed and will be < 0 if the paragraph
     * direction is right-to-left.
     */
    private static float drawUniformRun(Canvas canvas,
                                        Spanned text, int start, int end,
                                        float x, int top, int y, int bottom,
                                        Paint.FontMetricsInt fmi,
                                        TextPaint paint,
                                        TextPaint workPaint,
                                        boolean needWidth) {
        float ret = 0;
        CharacterStyle[] spans = text.getSpans(start, end, CharacterStyle.class);

        ReplacementSpan replacement = null;

        // XXX: This shouldn't be modifying paint, only workPaint.
        // However, the members belonging to TextPaint should have default
        // values anyway.  Better to ensure this in the Layout constructor.
        paint.bgColor = 0;
        paint.baselineShift = 0;
        workPaint.set(paint);

		if (spans.length > 0) {
			for (int i = 0; i < spans.length; i++) {
				CharacterStyle span = spans[i];

				if (span instanceof ReplacementSpan) {
					replacement = (ReplacementSpan)span;
				}
				else {
					span.updateDrawState(workPaint);
				}
			}
		}

        if (replacement == null) {
            CharSequence tmp;
            int tmpstart, tmpend;
            tmp = text;
            tmpstart = start;
            tmpend = end;
            if (fmi != null) {
                workPaint.getFontMetricsInt(fmi);
            }

            if (canvas != null) {
                if (workPaint.bgColor != 0) {
                    int c = workPaint.getColor();
                    Paint.Style s = workPaint.getStyle();
                    workPaint.setColor(workPaint.bgColor);
                    workPaint.setStyle(Paint.Style.FILL);

                    ret = workPaint.measureText(tmp, tmpstart, tmpend);

                    canvas.drawRect(x, top, x + ret, bottom, workPaint);

                    workPaint.setStyle(s);
                    workPaint.setColor(c);
                }

                if (needWidth) {
                    ret = workPaint.measureText(tmp, tmpstart, tmpend);
                }

                canvas.drawText(tmp, tmpstart, tmpend,
                                x, y + workPaint.baselineShift, workPaint);
            } else {
                if (needWidth) {
                    ret = workPaint.measureText(tmp, tmpstart, tmpend);
                }
            }
        } else {
            ret = replacement.getSize(workPaint, text, start, end, fmi);

            if (canvas != null) {
                replacement.draw(canvas, text, start, end,
                        x, top, y, bottom, workPaint);
            }
        }
        return ret;
    }

    /**
     * Returns the advance widths for a uniform left-to-right run of text with
     * no style changes in the middle of the run. If any style is replacement
     * text, the first character will get the width of the replacement and the
     * remaining characters will get a width of 0.
     * 
     * @param paint the paint, will not be modified
     * @param workPaint a paint to modify; on return will reflect the original
     *        paint plus the effect of all spans on the run
     * @param text the text
     * @param start the start of the run
     * @param end the limit of the run
     * @param widths array to receive the advance widths of the characters. Must
     *        be at least a large as (end - start).
     * @param fmi FontMetrics information; can be null
     * @return the actual number of widths returned
     */
    public static int getTextWidths(TextPaint paint,
                                    TextPaint workPaint,
                                    Spanned text, int start, int end,
                                    float[] widths, Paint.FontMetricsInt fmi) {
        MetricAffectingSpan[] spans = text.getSpans(start, end, MetricAffectingSpan.class);

		ReplacementSpan replacement = null;
        workPaint.set(paint);
		
		for (int i = 0; i < spans.length; i++) {
			MetricAffectingSpan span = spans[i];
			if (span instanceof ReplacementSpan) {
				replacement = (ReplacementSpan)span;
			}
			else {
				span.updateMeasureState(workPaint);
			}
		}
	
        if (replacement == null) {
            workPaint.getFontMetricsInt(fmi);
            workPaint.getTextWidths(text, start, end, widths);
        } else {
            int wid = replacement.getSize(workPaint, text, start, end, fmi);
            if (end > start) {
                widths[0] = wid;
                for (int i = start + 1; i < end; i++)
                    widths[i - start] = 0;
            }
        }
        return end - start;
    }

    /**
     * Renders and/or measures a directional run of text on a single line.
     * Unlike {@link #drawUniformRun}, this can render runs that cross style
     * boundaries.  Returns the signed advance width, if requested.
     *
     * <p>The x position is the leading edge of the text. In a right-to-left
     * paragraph, this will be to the right of the text to be drawn. Paint
     * should not have an Align value other than LEFT or positioning will get
     * confused.
     *
     * <p>This optimizes for unstyled text and so workPaint might not be
     * modified by this call.
     *
     * <p>The returned advance width will be < 0 if the paragraph
     * direction is right-to-left.
     */
    private static float drawDirectionalRun(Canvas canvas,
                                 CharSequence text, int start, int end,
                                 float x, int top, int y, int bottom,
                                 Paint.FontMetricsInt fmi,
                                 TextPaint paint,
                                 TextPaint workPaint,
                                 boolean needWidth) {

        // XXX: It looks like all calls to this API match dir and runIsRtl, so
        // having both parameters is redundant and confusing.

        // fast path for unstyled text
        if (!(text instanceof Spanned)) {
            float ret = 0;
            if (needWidth)
                ret = paint.measureText(text, start, end);
            if (canvas != null)
                canvas.drawText(text, start, end, x, y, paint);

            if (fmi != null) {
                paint.getFontMetricsInt(fmi);
            }
            return ret;
        }
        
        float ox = x;
        int minAscent = 0, maxDescent = 0, minTop = 0, maxBottom = 0;

        Spanned sp = (Spanned) text;
        Class<?> division;

        if (canvas == null)
            division = MetricAffectingSpan.class;
        else
            division = CharacterStyle.class;

        int next;
        for (int i = start; i < end; i = next) {
            next = sp.nextSpanTransition(i, end, division);

            // XXX: if dir and runIsRtl were not the same, this would draw
            // spans in the wrong order, but no one appears to call it this
            // way.
            x += drawUniformRun(canvas, sp, i, next,
                  x, top, y, bottom, fmi, paint, workPaint,
                  needWidth || next != end);

            if (fmi != null) {
                if (fmi.ascent < minAscent)
                    minAscent = fmi.ascent;
                if (fmi.descent > maxDescent)
                    maxDescent = fmi.descent;

                if (fmi.top < minTop)
                    minTop = fmi.top;
                if (fmi.bottom > maxBottom)
                    maxBottom = fmi.bottom;
            }
        }

        if (fmi != null) {
            if (start == end) {
                paint.getFontMetricsInt(fmi);
            } else {
                fmi.ascent = minAscent;
                fmi.descent = maxDescent;
                fmi.top = minTop;
                fmi.bottom = maxBottom;
            }
        }

        return x - ox;
    }

    /**
     * Draws a unidirectional run of text on a single line, and optionally
     * returns the signed advance.  Unlike drawDirectionalRun, the paragraph
     * direction and run direction can be different.
     */
    public static float drawText(Canvas canvas,
                                       CharSequence text, int start, int end,
                                       float x, int top, int y, int bottom,
                                       TextPaint paint,
                                       TextPaint workPaint,
                                       boolean needWidth) {
        return drawDirectionalRun(canvas, text, start, end,
                       x, top, y, bottom, null, paint, workPaint,
                       needWidth);
    }
    
    /**
     * Returns the width of a run of left-to-right text on a single line,
     * considering style information in the text (e.g. even when text is an
     * instance of {@link Spanned}, this method correctly measures
     * the width of the text).
     * 
     * @param paint the main {@link TextPaint} object; will not be modified
     * @param workPaint the {@link TextPaint} object available for modification;
     *        will not necessarily be used
     * @param text the text to measure
     * @param start the index of the first character to start measuring
     * @param end 1 beyond the index of the last character to measure
     * @param fmi FontMetrics information; can be null
     * @return The width of the text
     */
    public static float measureText(TextPaint paint,
                                    TextPaint workPaint,
                                    CharSequence text, int start, int end,
                                    Paint.FontMetricsInt fmi) {
        return drawDirectionalRun(null, text, start, end,
                       0, 0, 0, 0, fmi, paint, workPaint, true);
    }
}
