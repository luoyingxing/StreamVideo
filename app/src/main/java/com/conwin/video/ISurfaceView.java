package com.conwin.video;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.UnsupportedEncodingException;

/**
 * author:  luoyingxing
 * date: 2018/5/25.
 */
public class ISurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder holder;
    private RenderThread renderThread;
    private boolean isDraw = false;// 控制绘制的开关

    private Paint mPaint;

    public ISurfaceView(Context context) {
        super(context);
        init();
    }

    public ISurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ISurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ISurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        holder = this.getHolder();
        holder.addCallback(this);

        renderThread = new RenderThread();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    private byte[] image;
    private byte[] header;

    public void stuffImage(byte[] image) {
        this.image = image;
    }

    public void stuffHeader(byte[] header) {
        this.header = header;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isDraw = true;
        renderThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isDraw = false;
    }

    /**
     * 绘制界面的线程
     *
     * @author Administrator
     */
    private class RenderThread extends Thread {
        @Override
        public void run() {
            // 不停绘制界面
            while (isDraw) {
                drawUI();
            }
            super.run();
        }
    }

    /**
     * 界面绘制
     */
    public void drawUI() {
        Canvas canvas = holder.lockCanvas();
        try {
            drawCanvas(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawCanvas(Canvas canvas) {
        // 在 canvas 上绘制需要的图形

        //绘制图片
        if (null != image) {
            Rect dst = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
            canvas.drawBitmap(BitmapFactory.decodeByteArray(image, 0, image.length), null, dst, mPaint);
        }

        //绘制时间
        if (null != header) {
            String string = null;
            try {
                string = new String(header, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (null != string) {
                String[] header = string.split("\n");

                if (header.length > 3) {
                    String times = header[3];
                    if (!TextUtils.isEmpty(times)) {
                        String time = times.substring(9, times.length());

                        mPaint.setColor(Color.YELLOW);
                        mPaint.setTextSize(40); //以px为单位
                        mPaint.setStrokeWidth(3);
                        mPaint.setTextAlign(Paint.Align.LEFT);

                        canvas.drawText(time, 10, 60, mPaint);
                    }
                }
            }
        }
    }
}