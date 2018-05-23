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

    //意思是从数组A第i个（即A[i]处，含A[i]）开始copy长度为length个的byte数据到数组B从第j个开始（即B[j]处，含B[j]）覆盖！
//                    System.arraycopy(Object src, int srcPos, Object dest, int destPos, int length);

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

//            Log.e("---------- ", "conn: " + conn.toString());

//            Map<String, List<String>> map = conn.getHeaderFields();
//
//            for (String key : map.keySet()) {
//                Log.i("---------Header--- ", key + " : " + map.get(key));
//            }

//            Log.d("---------- ", "map: " + conn.getHeaderFields().toString());

            //得到响应流
            InputStream inputStream = conn.getInputStream();


            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                //缓存流数据，等待分割
                byte[] cache = new byte[1024 * 1000];
                //当前存取的下标
                int index = 0;
                //每次读取的大小
                byte[] read = new byte[1];

                while (inputStream.read(read) != -1) {
//                    Log.e("----inputStream------ ", new String(read, "UTF-8"));

                    //TODO 写入数据后
                    if (index < cache.length) {
                        cache[index] = read[0];
                        index++;
                    } else {
                        Log.e("----warning----", "缓冲区内存不足！");
                    }


//                    Log.d("----cache------ ", new String(cache));

                    //TODO 写完数据后，开始查找分隔符 regex
                    byte[] regexBytes = regex.getBytes();
                    //TODO 分隔符的起始坐标
                    int findIndex = -1;


                    for (int i = 0; i < index; i++) {

                        if (cache[i] == regexBytes[0]) {
                            int count = 0;

                            for (int j = 0; j < regexBytes.length; j++) {
                                if (cache[j + i] == regexBytes[j]) {
                                    count++;
                                } else {
                                    break;
                                }
                            }

                            if (count == regexBytes.length) {
                                //have find
                                findIndex = i;
                                break;
                            }
                        }
                    }

                    //TODO 进行分割数据         块数据 findIndex 块数据
                    if (findIndex != -1) {
                        //1. 先取出第一块数据
                        if (findIndex != 0) {
                            byte[] block = new byte[findIndex];
                            //取出块数据
                            for (int k = 0; k < findIndex; k++) {
                                block[k] = cache[k];
                            }


//                            Log.d("---块数据---find in -> " + findIndex, new String(block, "UTF-8"));

                            //TODO 将块数据进行分割
                            byte[] splitBytes = "\r\n\r\n".getBytes();

                            if (splitBytes.length <= block.length) {
                                for (int n = 0; n < block.length; n++) {

                                    if (block[n] == splitBytes[0]) {
                                        int count = 0;

                                        if (block.length - n >= splitBytes.length) {

                                            for (int m = 0; m < splitBytes.length; m++) {
                                                if (block[n + m] == splitBytes[m]) {
                                                    count++;
                                                } else {
                                                    break;
                                                }
                                            }

                                            if (count == splitBytes.length) {
                                                //have find image data

                                                byte[] image = new byte[block.length - n - splitBytes.length];

                                                int imageIndex = n + splitBytes.length;
                                                for (int w = 0; w < image.length; w++) {
                                                    image[w] = block[w + imageIndex];
                                                }

                                                //send image

                                                Message msg = handler.obtainMessage();
                                                msg.obj = getBitmapFromByte(image);
                                                handler.sendMessage(msg);
                                            }
                                        }
                                    }
                                }
                            }


                            // TODO ---- 在此之间解析图片数据 ----
                        }

                        //2. 去掉前半段数据
                        //TODO 切记切掉分隔符
                        int startIndex = findIndex + regexBytes.length;
                        byte[] split = new byte[cache.length - startIndex];

                        for (int p = startIndex; p < cache.length; p++) {
                            split[p - startIndex] = cache[p];
                        }

                        for (int q = 0; q < split.length; q++) {
                            cache[q] = split[q];
                        }

//                        Log.d("----cache------ ", new String(cache, "UTF-8"));

                        //重置继续赋值的下标
                        index = findIndex;


                    }

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
//    private String byte2hex(byte[] b) {
//        StringBuffer sb = new StringBuffer();
//        String tmp = "";
//        for (int i = 0; i < b.length; i++) {
//            tmp = Integer.toHexString(b[i] & 0XFF);
//            if (tmp.length() == 1) {
//                sb.append("0" + tmp);
//            } else {
//                sb.append(tmp);
//            }
//        }
//        return sb.toString();
//    }

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