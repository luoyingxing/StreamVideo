package com.conwin.video;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.conwin.video.jni.Test;

public class MainActivity extends AppCompatActivity {

    private String videoURL = "http://test.jingyun.cn:27000/stream/read?flag=1&streamid=W545ee6ET7mfLimcg+Zv9A";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = findViewById(R.id.sample_text);
        tv.setText(new Test().stringFromJNI());
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }


}