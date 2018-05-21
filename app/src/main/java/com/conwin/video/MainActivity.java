package com.conwin.video;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.conwin.video.jni.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private TextView tv;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.sample_text);
        imageView = findViewById(R.id.image_view);

        tv.setText(new Test().stringFromJNI());

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageStream();
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bitmap bitmap = (Bitmap) msg.obj;

            if (null != bitmap) {
                imageView.setImageBitmap(bitmap);
            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getImageStream();
    }

    private void getImageStream() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                request();
            }
        }).start();
    }

    private String videoURL = "http://test.jingyun.cn:27000/stream/read?flag=0&from=2018-05-18%2020:37:20.0&streamid=";

    private void request() {
        try {

            String streamId = URLEncoder.encode("HS9T-ukyShecApu7xTF7SA");

            URL url = new URL(videoURL + streamId);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setReadTimeout(30 * 1000); // 缓存的最长时间  
//            conn.setDoOutput(true);
//            conn.setUseCaches(false);
//            conn.setDoInput(true);

            conn.setRequestProperty("Connection", "Keep-Alive");
//            conn.setRequestProperty("Connection", "zh-cn");
            conn.setRequestProperty("Charsert", "UTF-8");

//            conn.setRequestProperty("Transfer-Encoding", "chunked");
//            conn.setRequestProperty("Trailer", "finish-flag");
//            conn.setRequestProperty("Content-type", "multipart/form-data;boundary=----WebKitFormBoundaryIZDrYHwuf2VJdpHw");

            conn.setRequestMethod("GET");

            conn.connect();

            Log.e("---------- ", "conn: " + conn.toString());

            Map<String, List<String>> map = conn.getHeaderFields();

            for (String key : map.keySet()) {
                Log.i("---------Header--- ", key + " : " + map.get(key));
            }

            Log.e("---------- ", "map: " + map.toString());

            //得到响应流
            InputStream inputStream = conn.getInputStream();

            int responseCode = conn.getResponseCode();

            StringBuilder builder = new StringBuilder();


            if (responseCode == HttpURLConnection.HTTP_OK) {

                byte[] data = new byte[1024];
                StringBuffer sb = new StringBuffer();

                while (inputStream.read(data) != -1) {
                    String s = new String(data, "UTF-8");
//                    sb.append(s);

                    Log.d("-------- data --> ", s);

//                    Log.d("---------- ", "二进制数据: " + byte2hex(data));


//                    String[] str = s.split("\r\n\r\n");
//
//                    for (String string : str) {
//                        Log.d("----------> ", string);
//                        if (string.contains("Content-Type")) {
//                            break;
//                        }
//
//                        Message msg = handler.obtainMessage();
//                        msg.obj = convertStringToIcon(string);
//                        handler.sendMessage(msg);
//                    }

//                    Message msg = handler.obtainMessage();
//                    msg.obj = getBitmapFromByte(data);
//                    handler.sendMessage(msg);

                }

                inputStream.close();
            }

            conn.disconnect();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 二进制转字符串
    private String byte2hex(byte[] b) {
        StringBuffer sb = new StringBuffer();
        String tmp = "";
        for (int i = 0; i < b.length; i++) {
            tmp = Integer.toHexString(b[i] & 0XFF);
            if (tmp.length() == 1) {
                sb.append("0" + tmp);
            } else {
                sb.append(tmp);
            }
        }
        return sb.toString();
    }

    public Bitmap getBitmapFromByte(byte[] temp) {
        if (temp != null) {
            return BitmapFactory.decodeByteArray(temp, 0, temp.length);
        } else {
            return null;
        }
    }

    /**
     * string转成bitmap
     *
     * @param st
     */
    public Bitmap convertStringToIcon(String st) {
        Bitmap bitmap;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(st, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }
}