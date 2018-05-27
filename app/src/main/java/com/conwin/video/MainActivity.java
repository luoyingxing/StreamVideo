package com.conwin.video;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.conwin.video.jni.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private TextView tv;
    private ImageView imageView;
    private ISurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.sample_text);
        imageView = findViewById(R.id.image_view);
        surfaceView = findViewById(R.id.surface);

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

            switch (msg.what) {
                case 601:
                    //image
                    Bitmap bitmap = (Bitmap) msg.obj;

                    if (null != bitmap) {
                        imageView.setImageBitmap(bitmap);
                    }

                    break;
                case 602:
                    //header
                    /**
                     * Content-Disposition: form-data; name=<field-name>;filename=<filename>
                     * Content-Type: application/octet-stream
                     * datetime: yyyy-MM-dd hh:mm:ss.S
                     * timestamp: 绝对时间戳(ms)
                     * Content-Length: <byte-size>
                     */
                    String string = (String) msg.obj;
                    Log.w("header", string);
                    String[] header = string.split("\n");

                    if (header.length > 3) {
                        String time = header[3];
                        if (!TextUtils.isEmpty(time)) {
                            tv.setText(time.substring(9, time.length()));
                        }
                    }

                    break;
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
                requestForBuffer();
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

//            Map<String, List<String>> map = conn.getHeaderFields();

            //得到响应流
            InputStream inputStream = conn.getInputStream();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                //缓存流数据，等待分割
                byte[] cache = new byte[1024 * 1024 * 8];
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

                        System.arraycopy(cache, startIndex, cache, 0, cache.length - startIndex);

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
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean mStop = false;

    private void requestForBuffer() {
        try {
            String streamId = URLEncoder.encode("HS9T-ukyShecApu7xTF7SA");
            URL url = new URL(videoURL + streamId);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30 * 1000); // 缓存的最长时间
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestMethod("GET");
            conn.connect();
            InputStream inputStream = conn.getInputStream();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                //缓存流数据，等待分割
                ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024 * 8);

                //TODO 写完数据后，开始查找分隔符 regex
                byte[] regexBytes = regex.getBytes();
                int regexLen = regexBytes.length;

                //TODO 将块数据进行分割
                byte[] splitBytes = "\r\n\r\n".getBytes();
                int splitLen = splitBytes.length;

                while (!mStop) {
                    int len = inputStream.available();

                    if (len != -1) {
                        //每次读取的大小
                        byte[] read = new byte[len];
                        int res = inputStream.read(read);

                        if (read.length != 0) {  //为0没意义
                            buffer.put(read);

                            //记录当前读写位置，相当于有效的数据长度为 position
                            int curPosition = buffer.position();

                            //TODO 分隔符的起始坐标
                            int findIndex = -1;

                            for (int i = 0; i < buffer.position(); i++) {

                                if (buffer.get(i) == regexBytes[0]) {
                                    int count = 0;

                                    for (int j = 0; j < regexLen; j++) {
                                        if (buffer.get(j + i) == regexBytes[j]) {
                                            count++;
                                        } else {
                                            break;
                                        }
                                    }

                                    if (count == regexLen) {
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
                                    buffer.position(0);
                                    buffer.mark();
                                    buffer.get(block, 0, findIndex);


//                                    Log.i("----块数据------ ", new String(block));


                                    if (block.length >= splitLen) { //若小于，没法比较
                                        for (int n = 0; n < block.length; n++) {

                                            if (block[n] == splitBytes[0]) {

                                                int count = 0;

                                                if (block.length - n >= splitLen) {
                                                    for (int m = 0; m < splitLen; m++) {
                                                        if (block[n + m] == splitBytes[m]) {
                                                            count++;
                                                        } else {
                                                            break;
                                                        }
                                                    }
                                                }

                                                if (count == splitBytes.length) {
                                                    //have find image data
                                                    byte[] image = new byte[block.length - n - splitBytes.length];
                                                    System.arraycopy(block, n + splitBytes.length, image, 0, image.length);

                                                    //send image
//                                                    parserImageByte(image);


                                                    byte[] header = new byte[n];
                                                    System.arraycopy(block, 0, header, 0, header.length);

                                                    //send header text
//                                                    parserHeaderByte(header);

                                                    surfaceView.stuff(header, image);
                                                }
                                            }
                                        }
                                    }

                                    //TODO 将后半段数据往前挪

                                    byte[] others = new byte[curPosition - findIndex - regexBytes.length];
                                    //取出块数据
                                    buffer.position(findIndex + regexBytes.length);
                                    buffer.mark();
                                    buffer.get(others, 0, curPosition - findIndex - regexBytes.length);

                                    buffer.clear();
                                    buffer.put(others);

                                }

                            }
                        }

                    }

                }

                inputStream.close();
            }
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析图片流数据
     *
     * @param image byte
     */
    private void parserImageByte(byte[] image) {
        Message msg = handler.obtainMessage();
        msg.what = 601;
        msg.obj = BitmapFactory.decodeByteArray(image, 0, image.length);
        handler.sendMessage(msg);
    }

    /**
     * 解析块头数据
     *
     * @param header byte
     */
    private void parserHeaderByte(byte[] header) {
        try {
            String str = new String(header, "UTF-8");

            Message msg = handler.obtainMessage();
            msg.what = 602;
            msg.obj = str;
            handler.sendMessage(msg);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}