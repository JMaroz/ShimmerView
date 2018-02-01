package com.marozzi.shimmerview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by amarozzi on 13/09/2017.
 */

public class ShimmerView extends View implements ValueAnimator.AnimatorUpdateListener {

    private static final int SHAPE_TYPE_SQUARE = 0;
    private static final int SHAPE_TYPE_OVAL = 1;

    private static final int CENTER_ALPHA = 50;
    private static final int EDGE_ALPHA = 12;
    private static final int SHADER_COLOR_R = 170;
    private static final int SHADER_COLOR_G = 170;
    private static final int SHADER_COLOR_B = 170;
    private static final int CENTER_COLOR = Color.argb(CENTER_ALPHA, SHADER_COLOR_R, SHADER_COLOR_G, SHADER_COLOR_B);
    private static final int EDGE_COLOR = Color.argb(EDGE_ALPHA, SHADER_COLOR_R, SHADER_COLOR_G, SHADER_COLOR_B);

    private static final int DEFAULT_CORNER_RADIUS = 0;
    private static final int DEFAULT_SHAPE_COLOR = EDGE_COLOR;

    private static final int ANIMATION_DURATION = 1500;

    private int shapeType;
    private int shapeColor;
    private float shapeCorner;

    private RectF rectF;
    private Bitmap shape;
    private Paint shaderPaint;
    private int[] shaderColors;

    private ValueAnimator animator;

    public ShimmerView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public ShimmerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public ShimmerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShimmerView, defStyleAttr, 0);

        shapeCorner = a.getDimensionPixelSize(R.styleable.ShimmerView_sv_shape_square_corner, DEFAULT_CORNER_RADIUS);
        shapeColor = a.getColor(R.styleable.ShimmerView_sv_shape_color, DEFAULT_SHAPE_COLOR);
        shapeType = a.getInt(R.styleable.ShimmerView_sv_shape_type, SHAPE_TYPE_SQUARE);

        a.recycle();

        shaderPaint = new Paint();
        shaderPaint.setAntiAlias(true);
        shaderColors = new int[]{EDGE_COLOR, CENTER_COLOR, EDGE_COLOR};

        animator = ValueAnimator.ofFloat(-1f, 2f);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(this);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        if (ViewCompat.isAttachedToWindow(this)) {
            float f = (Float) valueAnimator.getAnimatedValue();
            updateShader(getWidth(), f);
            invalidate();
        } else {
            animator.cancel();
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        switch (visibility) {
            case VISIBLE:
                animator.start();
                break;
            case INVISIBLE:
            case GONE:
                animator.cancel();
                break;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateShader(w, -1f);
        if (h > 0 && w > 0) {
            initShape(w, h);
        } else {
            animator.cancel();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (shape != null)
            canvas.drawBitmap(shape, 0, 0, null);

        if (animator.isRunning()) {
            if (shapeType == SHAPE_TYPE_SQUARE)
                canvas.drawRoundRect(rectF, shapeCorner, shapeCorner, shaderPaint);
            else
                canvas.drawOval(rectF, shaderPaint);
        }
    }

    private void updateShader(float w, float f) {
        float left = w * f;
        LinearGradient shader = new LinearGradient(left, 0f, left + w, 0f,
                shaderColors, new float[]{0f, .5f, 1f}, Shader.TileMode.CLAMP);
        shaderPaint.setShader(shader);
    }

    private void initShape(int w, int h) {
        if (shape != null) {
            shape.recycle();
            shape = null;
        }

        rectF = new RectF(0, 0, w, h);

        shape = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(shape);
        canvas.drawBitmap(getShape(w, h), 0, 0, null);
    }

    private Bitmap getShape(int w, int h) {
        Bitmap item = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(item);

        Paint itemPaint = new Paint();
        itemPaint.setAntiAlias(true);
        itemPaint.setColor(shapeColor);

        if (shapeType == SHAPE_TYPE_SQUARE)
            canvas.drawRoundRect(rectF, shapeCorner, shapeCorner, itemPaint);
        else
            canvas.drawOval(rectF, itemPaint);

        return item;
    }
}

