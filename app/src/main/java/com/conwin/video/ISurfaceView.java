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

    private boolean mEnableDraw = false;

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
        mPaint.setColor(Color.YELLOW);
        mPaint.setTextSize(40);
        mPaint.setStrokeWidth(3);
        mPaint.setTextAlign(Paint.Align.LEFT);

        holder = this.getHolder();
        holder.addCallback(this);
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

    public void stuff(byte[] header, byte[] image) {
        updateImage(header, image);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mEnableDraw = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mEnableDraw = false;
    }

    public void updateImage(byte[] header, byte[] image) {
        if (!mEnableDraw) {
            return;
        }

        Canvas canvas = holder.lockCanvas();
        try {
            drawCanvas(canvas, header, image);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    /**
     * @param canvas Canvas
     * @param header Content-Disposition: form-data; name=<field-name>;filename=<filename>
     *               Content-Type: application/octet-stream
     *               datetime: yyyy-MM-dd hh:mm:ss.S
     *               timestamp: 绝对时间戳(ms)
     *               Content-Length: <byte-size>
     * @param image  image data with byte
     */
    private void drawCanvas(Canvas canvas, byte[] header, byte[] image) {
        //draw image
        if (null != image) {
            Rect dst = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
            canvas.drawBitmap(BitmapFactory.decodeByteArray(image, 0, image.length), null, dst, mPaint);
        }

        //draw text of time
        if (null != header) {
            String string = null;
            try {
                string = new String(header, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (null != string) {
                String[] split = string.split("\n");

                if (split.length > 3) {
                    String times = split[3];
                    if (!TextUtils.isEmpty(times)) {
                        String time = times.substring(9, times.length());

                        canvas.drawText(time, 10, 60, mPaint);
                    }
                }
            }
        }
    }
}