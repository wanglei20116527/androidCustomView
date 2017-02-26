package com.wlbreath.fliplayout;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;

/**
 * Created by wlbreath on 17-1-6.
 */

public class FlipLayout extends FrameLayout {
    private ScrollView mScrollView;

    private boolean isFlipOut = false;
    private boolean isAnimating = false;
    private float mStartRawY = 0;
    private int mOffsetBaseLine = 0;
    private int mVelocityBaseLine = 0;
    private int mBackgroundColor = 0;
    private int mForegroundColor = 0;
    private int mFlipDuration = 0;
    private FlipOutListener mFlipOutListener = null;

    private VelocityTracker mVt = VelocityTracker.obtain();

    public FlipLayout(Context context){
        this(context, null);
    }

    public FlipLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlipLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FlipLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.FlipLayout,
                0, 0);

        mFlipDuration = typedArray.getInteger(R.styleable.FlipLayout_flipDuration, 250);
        mOffsetBaseLine = (int) typedArray.getDimension(R.styleable.FlipLayout_offsetBaseLine, 300);
        mVelocityBaseLine = (int) typedArray.getDimension(R.styleable.FlipLayout_velocityBaseLine, 5);
        mBackgroundColor = typedArray.getColor(R.styleable.FlipLayout_backgroundColor, 0x44000000);
        mForegroundColor = typedArray.getColor(R.styleable.FlipLayout_foregroundColor, 0xffffffff);

        this.setBackgroundColor(mBackgroundColor);
    }

    public void addOnFlipOutListener (@NonNull FlipOutListener listener) {
        mFlipOutListener = listener;
    }

    public void removeOnFlipOutListener () {
        mFlipOutListener = null;
    }

    public int getOffsetBaseLine() {
        return mOffsetBaseLine;
    }

    public void setOffsetBaseLine(int offsetBaseLine) {
        mOffsetBaseLine = offsetBaseLine;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        mBackgroundColor = color;
    }

    public int getForegroundColor() {
        return mForegroundColor;
    }

    public void setForegroundColor(int color) {
        mScrollView.setBackgroundColor(color);

        mForegroundColor = color;
    }

    public int getVelocityBaseLine() {
        return mVelocityBaseLine;
    }

    public void setVelocityBaseLine(int velocityBaseLine) {
        mVelocityBaseLine = velocityBaseLine;
    }

    public int getFlipDuration() {
        return mFlipDuration;
    }

    public void setFlipDuration(int duration) {
        this.mFlipDuration = duration;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mScrollView = new ScrollView(getContext());

        ScrollView.LayoutParams lp = new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.MATCH_PARENT);
        mScrollView.setLayoutParams(lp);
        mScrollView.setBackgroundColor(mForegroundColor);

        for (int i = this.getChildCount() - 1; i >= 0; --i) {
            View view = this.getChildAt(i);
            removeView(view);
            mScrollView.addView(view);

        }
        addView(mScrollView);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isAnimating || isFlipOut) {
            return super.onInterceptTouchEvent(ev);
        }

        boolean isIntercept = false;

        float curtViewH = getHeight();
        float contentH  = getScrollViewMaxContentHeight();

        float offsetY = 0;
        float scrollY = mScrollView.getScrollY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mVt.clear();
                mVt.addMovement(ev);
                mStartRawY  = ev.getRawY();

                isIntercept = false;
                break;

            case MotionEvent.ACTION_MOVE:
                mVt.addMovement(ev);

                offsetY = ev.getRawY() - mStartRawY;

                if (scrollY > 0) {
                    if (scrollY + curtViewH >= contentH - 0.5) {
                        isIntercept = offsetY < 0;
                    } else {
                        isIntercept = scrollY - offsetY < 0;
                    }
                } else {
                    isIntercept = offsetY > 0 || (offsetY < 0 && contentH < curtViewH);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mVt.addMovement(ev);
                isIntercept = mScrollView.getTranslationY() != 0;
                break;
        }
        return isIntercept || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isAnimating || isFlipOut) {
            return super.onTouchEvent(event);
        }

        boolean isComplete = false;

        float curtViewH = getHeight();
        float contentH  = getScrollViewMaxContentHeight();

        float offsetY = 0;
        float scrollY = mScrollView.getScrollY();
        float translationY = mScrollView.getTranslationY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartRawY  = event.getRawY();
                isComplete = true;
                break;

            case MotionEvent.ACTION_MOVE:
                offsetY = event.getRawY() - mStartRawY;

                if (scrollY > 0) {
                    if (scrollY + curtViewH >= contentH) {
                        if (offsetY < 0) {
                            mScrollView.setTranslationY(offsetY);
                            isComplete = true;
                        } else {
                            isComplete = false;
                        }
                    } else {
                        if (scrollY - offsetY < 0) {
                            mScrollView.setScrollY(0);
                            mScrollView.setTranslationY(offsetY - scrollY);
                            isComplete = true;
                        } else {
                            isComplete = false;
                        }
                    }
                } else {
                    if (offsetY > 0 || (offsetY < 0 && contentH < curtViewH)) {
                        mScrollView.setTranslationY(offsetY);
                        isComplete = true;
                    } else {
                        mScrollView.setTranslationY(0);
                        isComplete = false;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                mVt.computeCurrentVelocity(1);

                if (Math.abs(translationY) > mOffsetBaseLine
                        || mVt.getYVelocity() > mVelocityBaseLine) {
                    animateTranslationY(
                            translationY,
                            translationY > 0 ? curtViewH : -curtViewH,
                            true,
                            translationY > 0 ? FLIP_OUT_DIRECTION.FROM_UP: FLIP_OUT_DIRECTION.FROM_BOTTOM);
                    isFlipOut = true;

                } else {
                    animateTranslationY(translationY, 0);
                }

                isComplete = true;
                break;

            case MotionEvent.ACTION_CANCEL:
                animateTranslationY(translationY, 0);

                isComplete = true;
                break;
        }

        return isComplete || super.onTouchEvent(event);
    }

    private void animateTranslationY (float startY, float endY) {
       animateTranslationY(startY, endY, false, FLIP_OUT_DIRECTION.FROM_UP);
    }

    private void animateTranslationY (float startY, float endY, final boolean isFlipOut, final FLIP_OUT_DIRECTION direction) {
        float startValue = startY;
        float endValue = endY;

        ValueAnimator animator = ValueAnimator.ofFloat(startValue, endValue).setDuration(mFlipDuration);

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (isFlipOut && mFlipOutListener != null) {
                    if (direction == FLIP_OUT_DIRECTION.FROM_UP) {
                        mFlipOutListener.onFlipOutFromUpStart(FlipLayout.this);
                    } else {
                        mFlipOutListener.onFlipOutFromBottomStart(FlipLayout.this);
                    }
                }

                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isFlipOut && mFlipOutListener != null) {
                    if (direction == FLIP_OUT_DIRECTION.FROM_UP) {
                        mFlipOutListener.onFlipOutFromUpEnd(FlipLayout.this);
                    } else {
                        mFlipOutListener.onFlipOutFromBottomEnd(FlipLayout.this);
                    }
                }

                isAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mScrollView.setTranslationY((Float) animation.getAnimatedValue());
            }
        });

        animator.start();
    }

    private int getScrollViewMaxContentHeight () {
        int height = 0;

        for (int i = 0, len = mScrollView.getChildCount(); i < len; ++i) {
            View childView = mScrollView.getChildAt(i);
            int tmpHeight  = childView.getHeight();

            if (tmpHeight > height) {
                height = tmpHeight;
            }
        }

        return height;
    }

    public static enum FLIP_OUT_DIRECTION {
        FROM_UP,
        FROM_BOTTOM
    };

    public static interface FlipOutListener {
        void onFlipOutFromUpStart(FlipLayout flipLayout);
        void onFlipOutFromUpEnd(FlipLayout flipLayout);
        void onFlipOutFromBottomStart(FlipLayout flipLayout);
        void onFlipOutFromBottomEnd(FlipLayout flipLayout);
    }
}
