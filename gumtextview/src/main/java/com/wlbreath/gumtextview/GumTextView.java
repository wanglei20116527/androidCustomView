package com.wlbreath.gumtextview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.AbsoluteLayout;
import android.widget.TextView;

/**
 * Created by wlbreath on 17-1-6.
 */

public class GumTextView extends AbsoluteLayout implements View.OnTouchListener {
    private TextView mTextView;

    private float mCx = 0;
    private float mCy = 0;
    private float mRadius = 0;
    private float mMinRadius = 0;
    private float mMaxOffset = 0;
    private String mText = "";
    private float mTextSize = 0;
    private int mTextColor = 0xFFFFFFFF;
    private int mBackgroundColor = 0xFF000000;

    private OnGumBreakListener mOnGumBreakListener = null;

    private boolean mIsDragging = false;
    private boolean mIsAnimating = false;
    private boolean mGumBreak = false;
    private float mStartX = 0;
    private float mStartY = 0;
    private float mEndX = 0;
    private float mEndY = 0;


    public GumTextView(Context context){
        this(context, null);
    }

    public GumTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GumTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GumTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.GumTextView,
                0, 0);

        mRadius = typedArray.getDimension(R.styleable.GumTextView_radius, 30);
        mMinRadius = typedArray.getDimension(R.styleable.GumTextView_minRadius, 15);
        mMaxOffset = typedArray.getDimension(R.styleable.GumTextView_maxOffset, 300);
        mText = typedArray.getString(R.styleable.GumTextView_text);
        mTextSize  = typedArray.getDimension(R.styleable.GumTextView_textSize, 23);
        mTextColor = typedArray.getColor(R.styleable.GumTextView_textColor, 0xffffffff);
        mBackgroundColor = typedArray.getColor(R.styleable.GumTextView_backgroundColor, 0xff000000);

        mTextView = new TextView(context);
        mTextView.setEllipsize(TextUtils.TruncateAt.END);
        mTextView.setText(mText);
        mTextView.setTextColor(mTextColor);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        mTextView.setSingleLine();
        mTextView.setBackgroundResource(R.drawable.circle);
        GradientDrawable bg = (GradientDrawable) mTextView.getBackground();
        bg.setColor(mBackgroundColor);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setOnTouchListener(this);
        mTextView.post(new Runnable() {
            @Override
            public void run() {

                int width  = GumTextView.this.getWidth();
                int height = GumTextView.this.getHeight();

                mCx = width / 2;
                mCy = height / 2;

                GradientDrawable bg = (GradientDrawable) mTextView.getBackground();
                bg.setSize(width, height);
            }
        });

        addView(mTextView);
    }

    public void setOnGumBreakListener (@NonNull OnGumBreakListener listener) {
        mOnGumBreakListener = listener;
    }

    public void offOnGumBreakListener () {
        mOnGumBreakListener = null;
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float size) {
        this.mTextSize = size;
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int color) {
        this.mTextColor = color;
        mTextView.setTextColor(color);
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
        mTextView.setText(text);
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(int color) {
        this.mBackgroundColor = color;

        GradientDrawable bg = (GradientDrawable) mTextView.getBackground();
        bg.setColor(mBackgroundColor);
    }

    public float getMaxOffset() {
        return mMaxOffset;
    }

    public void setMaxOffset(float maxOffset) {
        this.mMaxOffset = maxOffset;
    }

    public float getMinRadius() {
        return mMinRadius;
    }

    public void setMinRadius(float minRadius) {
        this.mMinRadius = minRadius;
    }

    public float getRadius() {
        return mRadius;
    }

    public void setRadius(float radius) {
        this.mRadius = radius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!mIsDragging && !mIsAnimating || mGumBreak) {
            return;
        }

        LayoutParams lp = (LayoutParams) mTextView.getLayoutParams();
        float offsetX = lp.x;
        float offsetY = lp.y;
        float offset = (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY);

        if (offset < mMaxOffset) {
            float r1  = mRadius - (mRadius - mMinRadius) * offset / mMaxOffset;
            float cx1 = mCx;
            float cy1 = mCy;

            float r2  = Math.min(mTextView.getWidth() / 2, mTextView.getHeight() / 2);
            float cx2 = mTextView.getLeft() + mTextView.getWidth() / 2;
            float cy2 = mTextView.getTop() + mTextView.getHeight() / 2;

            drawCircle(canvas, cx1, cy1, r1);
            drawJoint(canvas, cx1, cy1, r1, cx2, cy2, r2);
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mIsAnimating) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsDragging = true;

                mStartX = event.getRawX();
                mStartY = event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                float offsetX = event.getRawX() - mStartX;
                float offsetY = event.getRawY() - mStartY;
                float offset  = (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY);

                if (offset > mMaxOffset) {
                    mGumBreak = true;
                }

                updateTextViewPosition(offsetX, offsetY);

                invalidate();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mGumBreak) {
                    mTextView.setVisibility(INVISIBLE);
                    this.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mOnGumBreakListener != null) {
                                mOnGumBreakListener.onBreak(GumTextView.this);
                            }
                        }
                    });
                    return true;
                }

                mEndX = event.getRawX();
                mEndY = event.getRawY();

                ValueAnimator animator = ValueAnimator.ofFloat(0, 1).setDuration(750);

                animator.setInterpolator(new BounceInterpolator());

                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mIsAnimating = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mIsAnimating = false;
                        mIsDragging = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mIsAnimating = false;
                        mIsDragging = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float fraction = animation.getAnimatedFraction();

                        float x = (mEndX - mStartX) * (1 - fraction);
                        float y = (mEndY - mStartY) * (1 - fraction);

                        updateTextViewPosition(x, y);

                        invalidate();
                    }
                });

                animator.start();
                break;
        }

        return true;
    }

    private void updateTextViewPosition (float x, float y) {
        LayoutParams lp = (LayoutParams) mTextView.getLayoutParams();
        lp.x = (int) x;
        lp.y = (int) y;

        mTextView.setLayoutParams(lp);
    }

    private void drawCircle(Canvas canvas, float cx, float cy, float radius) {
        canvas.save();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(mBackgroundColor);
        canvas.drawCircle(cx, cy, radius, paint);

        canvas.restore();
    }

    private void drawJoint (Canvas canvas, float cx1, float cy1, float r1, float cx2, float cy2, float r2) {
        canvas.save();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(mBackgroundColor);

        float x = cx2 - cx1;
        float y = cy2 - cy1;
        float z = (float) Math.sqrt(x * x + y * y);

        float point1x = cx1 - y / z * r1;
        float point1y = cy1 + x / z * r1;

        float point2x = cx2 - y / z * r2;
        float point2y = cy2 + x / z * r2;

        float point3x = cx2 + y / z * r2;
        float point3y = cy2 - x / z * r2;

        float point4x = cx1 + y / z * r1;
        float point4y = cy1 - x / z * r1;

        float point5x = (cx1 + cx2) / 2;
        float point5y = (cy1 + cy2) / 2;


        Path path = new Path();
        path.moveTo(point1x, point1y);
        path.quadTo(point5x, point5y, point2x, point2y);
        path.lineTo(point3x, point3y);
        path.quadTo(point5x, point5y, point4x, point4y);
        path.moveTo(point1x, point1y);
        canvas.drawPath(path, paint);

        canvas.restore();
    }

    public interface OnGumBreakListener {
        void onBreak(View view);
    }
}
