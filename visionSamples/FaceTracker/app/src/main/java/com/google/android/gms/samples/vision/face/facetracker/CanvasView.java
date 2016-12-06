/**
 * CanvasView.java
 *
 * Copyright (c) 2014 Tomohiro IKEDA (Korilakkuma)
 * Released under the MIT license
 */

package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
// import android.util.Log;
// import android.widget.Toast;

/**
 * This class defines fields and methods for drawing.
 */
public class CanvasView extends View {

    private Context context = null;
    private Canvas canvas   = null;
    private Bitmap bitmap   = null;

    private List<Path> pathLists  = new ArrayList<Path>();
    private List<Paint> paintLists = new ArrayList<Paint>();

    // for Eraser
    private int baseColor = Color.parseColor("#FFFFFFFF");

    // for Paint
    private Paint.Style paintStyle = Paint.Style.STROKE;
    private int paintStrokeColor   = Color.parseColor("#FE2EC8");
    private int paintFillColor     = Color.parseColor("#FF00FF");
    private float paintStrokeWidth = 150F;
    private int opacity            = 200;
    private float blur             = 100F;
    private Paint.Cap lineCap      = Paint.Cap.ROUND;

    /**
     * Copy Constructor
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public CanvasView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setup(context);
    }

    /**
     * Copy Constructor
     *
     * @param context
     * @param attrs
     */
    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setup(context);
    }

    /**
     * Copy Constructor
     *
     * @param context
     */
    public CanvasView(Context context) {
        super(context);
        this.setup(context);
    }

    private void setup(Context context) {
        this.context = context;

        this.pathLists.add(new Path());
        this.paintLists.add(this.createPaint());
    }

    private Paint createPaint() {
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setStyle(this.paintStyle);
        paint.setStrokeWidth(this.paintStrokeWidth);
        paint.setStrokeCap(this.lineCap);
        paint.setStrokeJoin(Paint.Join.MITER);  // fixed

        paint.setColor(this.paintStrokeColor);
        paint.setShadowLayer(this.blur, 0F, 0F, this.paintStrokeColor);
        paint.setAlpha(this.opacity);

        return paint;
    }

    private Path createPath(float x1, float y1, float x2, float y2) {
        Path path = new Path();

        path.moveTo(x1, y1);
        path.lineTo(x2, y2);

        return path;
    }

    public void create(float x1, float y1, float x2, float y2) {
        clear();

        pathLists.add(createPath(x1, y1, x2, y2));
        paintLists.add(this.createPaint());
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Before "drawPath"
//        canvas.drawColor(this.baseColor);

        if (this.bitmap != null) {
            canvas.drawBitmap(this.bitmap, 0F, 0F, new Paint());
        }

        if (pathLists.size() > 0) {
            Path path = this.pathLists.get(0);
            Paint paint = this.paintLists.get(0);

            canvas.drawPath(path, paint);
        }

        this.canvas = canvas;
    }

    public void clear() {
        Path path = new Path();
        path.moveTo(0F, 0F);
        path.addRect(0F, 0F, 1000F, 1000F, Path.Direction.CCW);
        path.close();

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        if (pathLists.size() > 0) {
            this.pathLists.remove(0);
            this.paintLists.remove(0);
        }

        // Clear
        this.invalidate();
    }

    /**
     * This method gets current canvas as bitmap.
     *
     * @return This is returned as bitmap.
     */
    public Bitmap getBitmap() {
        this.setDrawingCacheEnabled(false);
        this.setDrawingCacheEnabled(true);

        return Bitmap.createBitmap(this.getDrawingCache());
    }

    /**
     * This method gets current canvas as scaled bitmap.
     *
     * @return This is returned as scaled bitmap.
     */
    public Bitmap getScaleBitmap(int w, int h) {
        this.setDrawingCacheEnabled(false);
        this.setDrawingCacheEnabled(true);

        return Bitmap.createScaledBitmap(this.getDrawingCache(), w, h, true);
    }

    /**
     * This method draws the designated bitmap to canvas.
     *
     * @param bitmap
     */
    public void drawBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.invalidate();
    }

    /**
     * This method draws the designated byte array of bitmap to canvas.
     *
     * @param byteArray This is returned as byte array of bitmap.
     */
    public void drawBitmap(byte[] byteArray) {
        this.drawBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));
    }

    /**
     * This static method gets the designated bitmap as byte array.
     *
     * @param bitmap
     * @param format
     * @param quality
     * @return This is returned as byte array of bitmap.
     */
    public static byte[] getBitmapAsByteArray(Bitmap bitmap, CompressFormat format, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * This method gets the bitmap as byte array.
     *
     * @param format
     * @param quality
     * @return This is returned as byte array of bitmap.
     */
    public byte[] getBitmapAsByteArray(CompressFormat format, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        this.getBitmap().compress(format, quality, byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * This method gets the bitmap as byte array.
     * Bitmap format is PNG, and quality is 100.
     *
     * @return This is returned as byte array of bitmap.
     */
    public byte[] getBitmapAsByteArray() {
        return this.getBitmapAsByteArray(CompressFormat.PNG, 100);
    }

}
