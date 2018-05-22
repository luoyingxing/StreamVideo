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
import java.util.Arrays;
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

            Log.w("-----bitmap-----> ", "" + bitmap);

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

    private String regex = "------WebKitFormBoundaryIZDrYHwuf2VJdpHw";

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
//            conn.setRequestProperty("Charsert", "UTF-8");

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

//            Log.e("---------- ", "map: " + map.toString());

            //得到响应流
            InputStream inputStream = conn.getInputStream();

            int responseCode = conn.getResponseCode();

//            StringBuilder cacheBuilder = new StringBuilder();

            if (responseCode == HttpURLConnection.HTTP_OK) {

                byte[] read = new byte[1024];

//                byte[] cache = new byte[1024 * 100];

                byte[] blockCache = new byte[0];

//                StringBuffer cache = new StringBuffer();

                byte[] regexByte = regex.getBytes();
                int index = 0;

                while (inputStream.read(read) != -1) {

//                    Log.e("----inputStream------ ", new String(read, "UTF-8"));

                    Log.d("----data block----", Arrays.toString(read));

//                    String s = new String(data, "UTF-8");

                    //累计到数组
                    byte[] cur = blockCache;
                    byte[] news = new byte[cur.length + read.length];

                    if (blockCache.length == 0) {
                        blockCache = read;
                    } else {
                        for (int i = cur.length; i < news.length; i++) {
                            news[i] = read[i - cur.length];
                        }

                        blockCache = news;

                    }


                    boolean find = false;
                    int flag = 0;

                    //TODO 寻找分隔符
                    for (int i = 0; i < blockCache.length; i++) {

                        if (blockCache[i] == regexByte[0]) {
                            int count = 0;

                            for (int j = 0; j < regexByte.length; j++) {

//                                Log.i("----for----", "i:" + i +
//                                        " j:" + j +
//                                        " regexByte.length:" + regexByte.length +
//                                        " read.length:" + read.length);

                                if (blockCache[i + j] == regexByte[j]) {
                                    count++;
                                }
                            }

                            if (count == regexByte.length) {
                                //TODO find
                                find = true;
                                flag = i;
                            }
                        }
                    }


                    if (find) {

                        byte[] cache = new byte[flag]; //块数据

                        for (int i = 0; i < flag; i++) {
                            cache[i] = blockCache[i];
                        }

                        //截掉前面一节
                        byte[] splitCache = new byte[blockCache.length - cache.length];

                        for (int i = 0; i < splitCache.length; i++) {
                            splitCache[i] = blockCache[i + flag];
                        }

                        blockCache = splitCache;

                        //TODO 块数据处理，寻找图片数据
                        //判断换行符进行分割

                        byte[] str = "\r\n\r\n".getBytes();

                        if (cache.length >= str.length) {

                            for (int k = 0; k < cache.length; k++) {
                                int count = 0;

                                if (cache[k] == str[0]) {

                                    for (int p = 0; p < str.length; p++) {
                                        if (cache[p + k] == str[p]) {
                                            count++;
                                        }
                                    }
                                }

                                if (count == str.length) {

                                    byte[] image = new byte[cache.length - k - str.length];

                                    for (int w = 0; w < image.length; w++) {
                                        image[w] = cache[w + k + str.length];
                                    }

                                    Message msg = handler.obtainMessage();
                                    msg.obj = getBitmapFromByte(image);
                                    handler.sendMessage(msg);
                                }
                            }

                        }


                    }


//                    boolean find = false;
//                    int flag = 0;
//
//                    //TODO 寻找分隔符
//                    for (int i = 0; i < read.length; i++) {
//                        flag = i;
//                        int count = 0;
//
//                        if (read[i] == regexByte[0]) {
//
//                            for (int j = 0; j < regexLength; j++) {
//
//                                if (read[j] == regexByte[j]) {
//                                    count++;
//                                }
//                            }
//                        }
//
//                        if (count == regexLength) {
//                            //TODO find
//                            find = true;
//                            index = i;
//                        }
//                    }
//
//
//                    if (find) {
//
//                        for (int i = cache.length - 1; i < read.length - flag + cache.length; i++) {
//                            cache[i] = (byte) (i);
//                        }
//
//
//                        //判断换行符进行分割
//
//                        byte[] str = "\r\n\r\n".getBytes();
//                        boolean findSplit = false;
//                        int split = 0;
//
//                        for (int k = 0; k < cache.length; k++) {
//
//                            int count = 0;
//
//                            if (cache[k] == str[0]) {
//
//                                for (int p = 0; p < str.length; p++) {
//                                    if (cache[p] == str[p]) {
//                                        count++;
//                                    }
//                                }
//                            }
//
//                            if (count == str.length) {
//                                split = k;
//                                findSplit = true;
//                            }
//                        }
//
//
//                        if (findSplit) {
//
//                            byte[] image = new byte[cache.length - split];
//
//                            for (int q = split; q < cache.length; q++) {
//                                image[q - split] = cache[q];
//                            }
//
//
//                            Message msg = handler.obtainMessage();
//                            msg.obj = getBitmapFromByte(image);
//                            handler.sendMessage(msg);
//
//                        }
//
//
//                    } else {
//
//                        for (int i = index; i < cache.length; i++) {
//                            cache[i] = (byte) (i);
//                        }
//                    }


//                    if (s.contains(regex)) {
//                        String[] str = s.trim().split(regex);
//
//                        if (str.length > 0) {
//                            cache.append(str[0]);
//                        }
//
//                        //TODO 检查是否已满足一块数据
//                        String blockData = cache.toString().trim();
////                        Log.v("块数据", blockData);
//
//                        String[] blocks = blockData.split("\r\n\r\n");
//
//                        if (blocks.length > 1) {
//                            String image = blocks[1].trim();
//
////                            Log.w("--block--header--", blocks[0]);
////                            Log.d("--block--image--", blocks[1]);
//
//                            Message msg = handler.obtainMessage();
//                            msg.obj = convertStringToIcon(image);
//                            handler.sendMessage(msg);
//                        }
//
//
//                        //TODO 清空数据
//                        cache.setLength(0);
//
//
//                        if (str.length > 1) {
//                            cache.append(str[1]);
//                        }
//
//
//                    } else {
//                        cache.append(s);
//                    }


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
            byte[] bitmapArray = st.getBytes("UTF-8");

//            Log.d("--Bitmap--", Arrays.toString(bitmapArray));

//            bitmapArray = Base64.decode(st.trim(), Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}