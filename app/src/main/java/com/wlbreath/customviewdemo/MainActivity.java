package com.wlbreath.customviewdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.wlbreath.fliplayout.FlipLayout;
import com.wlbreath.gumtextview.GumTextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void showGumTextViewDemo (View view) {
        Intent intent = new Intent(this, GumTextViewActivity.class);
        startActivity(intent);
    }

    public void showFlipLayoutDemo (View view) {
        Intent intent = new Intent(this, FlipLayoutDemoActivity.class);
        startActivity(intent);
    }

}
