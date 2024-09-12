package com.example.sketch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private Paint currentPaint;  // Holds the current paint color
    private Path currentPath;    // Holds the current path being drawn
    private List<DrawnPath> paths = new ArrayList<>();  // Keeps track of all paths and their colors
    private String title;
    private int originalWidth;
    private int originalHeight;

    private class DrawnPath {
        Path path;
        Paint paint;

        DrawnPath(Path path, Paint paint) {
            this.path = path;
            this.paint = new Paint(paint); // Make a copy to maintain original color
        }
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        currentPaint = new Paint();
        currentPaint.setAntiAlias(true);
        currentPaint.setColor(Color.BLACK);  // Default color
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeWidth(5f);

        currentPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (originalWidth == 0 && originalHeight == 0) {
            // Store the original canvas size the first time it's drawn
            originalWidth = w;
            originalHeight = h;
        }
    }

    // Method to resize the canvas and redraw
    public void resizeAndRedraw(double newWidth, double newHeight) {
        float scaleX = (float) newWidth / originalWidth;
        float scaleY = (float) newHeight / originalHeight;

        // Scale all paths
        for (DrawnPath drawnPath : paths) {
            drawnPath.path.transform(getScaleMatrix(scaleX , scaleY));
        }

        // Redraw with new size
        invalidate();
    }

    public Bitmap captureThumbnail(int width, int height) {
        // Create a bitmap with the given width and height
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Calculate scaling factors to fit the entire view in the thumbnail
        float scaleX = (float) width / getWidth();
        float scaleY = (float) height / getHeight();
        float scale = Math.min(scaleX,scaleY); // Scale to fit the thumbnail size

        // Scale the canvas
        canvas.scale( scale, scale);
        // Translate the canvas to center the view
        canvas.translate( (width - getWidth() * scale) ,  (height - getHeight() * scale));

        // Draw the view on the canvas
        draw(canvas);

        return bitmap;
    }


    private Matrix getScaleMatrix(float scaleX, float scaleY) {
        Matrix matrix = new Matrix();
        matrix.setScale(scaleX *2, scaleY *2);
        return matrix;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Get the view width and height
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // Calculate scaling factors based on the original content size
        float scaleX = (float) viewWidth / originalWidth;
        float scaleY = (float) viewHeight / originalHeight;

        // Use the smaller scale factor to maintain aspect ratio
        float scale = Math.min(scaleX, scaleY);

        // Apply scaling to the canvas to adjust all paths
        canvas.scale(scale, scale, viewWidth / 2, viewHeight / 2);

        // Draw all saved paths
        for (DrawnPath dp : paths) {
            canvas.drawPath(dp.path, dp.paint);
        }

        // Draw the current path
        canvas.drawPath(currentPath, currentPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                // When the user lifts their finger, save the current path and color
                paths.add(new DrawnPath(currentPath, currentPaint));
                currentPath = new Path();  // Create a new path for the next drawing
                break;
        }

        // Request to redraw the view
        invalidate();
        return true;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    // Method to change the current color
    public void setPaint(int color) {
        currentPaint.setColor(color);  // Set the paint color to the selected one
    }

    public void setPaintRadius(float brushSize) {
        currentPaint.setStrokeWidth(brushSize);
    }

    public void clearCanvas() {
        // Clear all the saved paths
        paths.clear();

        currentPath.reset();
        invalidate();
    }

}
