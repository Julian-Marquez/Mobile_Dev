package com.example.sketch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
    private String Title;


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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw all saved paths with their colors
        for (DrawnPath dp : paths) {
            canvas.drawPath(dp.path, dp.paint);
        }

        // Draw the current path being drawn
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

    public void setTitle(String title){
        this.Title = title;
    }

    public String getTitle(){
        return this.Title;
    }

    // Method to change the current color
    public void setPaint(int color) {
        currentPaint.setColor(color);  // Set the paint color to the selected one
    }

    public void setPaintRadius(float brushsize){
        currentPaint.setStrokeWidth(brushsize);
    }

    public void clearCanvas() {
        // Clear all the saved paths
        paths.clear();

        currentPath.reset();
        invalidate();
    }

}

