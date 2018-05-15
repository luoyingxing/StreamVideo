package com.conwin.video.jni;

/**
 * author:  luoyingxing
 * date: 2018/5/15.
 */
public class Test {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
