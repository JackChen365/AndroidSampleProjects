package com.cz.android.sample.animator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.cz.android.animator.version2.ObjectAnimator;
import com.cz.android.animator.version2.ValueAnimator;

import java.util.ArrayList;


public class AnimationView extends View implements ValueAnimator.AnimatorUpdateListener {
    public final ArrayList<ShapeHolder> balls = new ArrayList<ShapeHolder>();
    private ObjectAnimator animation;
    private float mDensity;

    public AnimationView(Context context) {
        super(context);

        mDensity = getContext().getResources().getDisplayMetrics().density;

        addBall(50f, 25f);
    }

    private void createAnimation() {
        if (animation == null) {
            ShapeHolder ball = balls.get(0);
            ObjectAnimator animDown = ObjectAnimator.ofFloat(ball, "y",
                    0f, getHeight() - ball.getHeight()).setDuration(500);
            animDown.setInterpolator(new AccelerateInterpolator());
            animDown.addUpdateListener(this);
            animation=animDown;
        }
    }

    private ShapeHolder addBall(float x, float y) {
        OvalShape circle = new OvalShape();
        circle.resize(50f * mDensity, 50f * mDensity);
        ShapeDrawable drawable = new ShapeDrawable(circle);
        ShapeHolder shapeHolder = new ShapeHolder(drawable);
        shapeHolder.setX(x - 25f);
        shapeHolder.setY(y - 25f);
        int red = (int)(100 + Math.random() * 155);
        int green = (int)(100 + Math.random() * 155);
        int blue = (int)(100 + Math.random() * 155);
        int color = 0xff000000 | red << 16 | green << 8 | blue;
        Paint paint = drawable.getPaint(); //new Paint(Paint.ANTI_ALIAS_FLAG);
        int darkColor = 0xff000000 | red/4 << 16 | green/4 << 8 | blue/4;
        RadialGradient gradient = new RadialGradient(37.5f, 12.5f,
                50f, color, darkColor, Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        shapeHolder.setPaint(paint);
        balls.add(shapeHolder);
        return shapeHolder;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < balls.size(); ++i) {
            ShapeHolder shapeHolder = balls.get(i);
            canvas.save();
            canvas.translate(shapeHolder.getX(), shapeHolder.getY());
            shapeHolder.getShape().draw(canvas);
            canvas.restore();
        }
    }

    public void startAnimation() {
        createAnimation();
        animation.start();
    }

    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }

}