package com.wlbreath.customviewdemo;

import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.wlbreath.fliplayout.FlipLayout;

public class FlipLayoutDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flip_layout_demo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        findViewById(R.id.testBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "test btn", Toast.LENGTH_SHORT).show();
            }
        });

        final FlipLayout flipLayout = (FlipLayout) findViewById(R.id.flipLayout);

        flipLayout.addOnFlipOutListener(new FlipLayout.FlipOutListener() {
            @Override
            public void onFlipOutFromUpStart(FlipLayout layout) {
                int alpha = flipLayout.getBackground().getAlpha();

                ValueAnimator animator = ValueAnimator.ofInt(alpha, 0).setDuration(250);

                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        flipLayout.getBackground().setAlpha((Integer) animation.getAnimatedValue());
                    }
                });

                animator.start();
            }

            @Override
            public void onFlipOutFromUpEnd(FlipLayout layout) {
                ViewGroup parent = (ViewGroup) flipLayout.getParent();
                parent.removeView(flipLayout);
            }

            @Override
            public void onFlipOutFromBottomStart(FlipLayout layout) {
                int alpha = flipLayout.getBackground().getAlpha();

                ValueAnimator animator = ValueAnimator.ofInt(alpha, 0).setDuration(250);

                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        flipLayout.getBackground().setAlpha((Integer) animation.getAnimatedValue());
                    }
                });

                animator.start();
            }

            @Override
            public void onFlipOutFromBottomEnd(FlipLayout layout) {
                ViewGroup parent = (ViewGroup) flipLayout.getParent();
                parent.removeView(flipLayout);
            }
        });

    }

}
