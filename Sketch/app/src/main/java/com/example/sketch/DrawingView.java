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

import com.google.android.material.shape.ShapePath;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {
    private Bitmap bitmapToRedraw;
    private Paint currentPaint;  // Holds the current paint color
    private Path currentPath;    // Holds the current path being drawn
    private List<DrawnPath> paths = new ArrayList<>();
    private List<DrawnShape> shapes = new ArrayList<>(); // Keeps track of all paths and their colors
    private String title;
    private int originalWidth;
    private int originalHeight;
    private float currentShapeSize = 100; // Default size for new shapes
    private String currentShapeType; // Default size for new shapes

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

    public void setBitmap(Bitmap bitmap) {
        this.bitmapToRedraw = bitmap;
        invalidate();  // Triggers a redraw with the new bitmap
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
    public void Redraw() {
        float scaleX = (float) originalWidth;
        float scaleY = (float) originalHeight;

        // Scale all paths
        for (DrawnPath drawnPath : paths) {
            drawnPath.path.transform(getScaleMatrix(scaleX, scaleY));
        }

        // Redraw with new size
        invalidate();
    }

    public Bitmap getFullcanvas() {

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        draw(canvas);
        return bitmap;
    }


    public Bitmap captureThumbnail(int width, int height) {
        // Create a bitmap with the given width and height

        Bitmap bitmap = Bitmap.createBitmap((int) (width * 1.75), (int) (height * 2), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Calculate scaling factors to fit the entire view in the thumbnail
        float scaleX = (float) width / getWidth();
        float scaleY = (float) height / getHeight();
        float scale = Math.min(scaleX, scaleY); // Scale to fit the thumbnail size

        // Scale the canvas
        canvas.scale((float) (scaleX * 1.57), scaleY * 2);
        // Translate the canvas to center the view
        canvas.translate((width - getWidth() * scale), (height - getHeight() * scale));

        // Draw the view on the canvas
        draw(canvas);

        return bitmap;
    }


    private Matrix getScaleMatrix(float scaleX, float scaleY) {
        Matrix matrix = new Matrix();
        matrix.setScale(scaleX * 2, scaleY * 2);
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

        if (bitmapToRedraw != null) {
            canvas.drawBitmap(bitmapToRedraw, 0, 0, null);
        }

        // Apply scaling to the canvas to adjust all paths
        canvas.scale(scale, scale, viewWidth / 2, viewHeight / 2);

        // Draw all saved paths
        for (DrawnPath dp : paths) {
            canvas.drawPath(dp.path, dp.paint);
        }
        // Draw shapes
        for (DrawnShape ds : shapes) {  // Assuming you have a List<DrawnShape> shapes
            canvas.drawPath(ds.path, ds.paint);
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
                // Check if the user is in "shape" mode (set currentShapeType to null for free drawing)
                if (currentShapeType != null) {
                    // Add the shape at the touch coordinates
                    addShape(currentShapeType, x, y, currentShapeSize);
                    invalidate();  // Request to redraw the view
                    return true;  // Stop further event handling as a shape is added
                } else {
                    // If no shape mode, start drawing freehand
                    currentPath.moveTo(x, y);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentShapeType == null) {
                    // Continue freehand drawing
                    currentPath.lineTo(x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (currentShapeType == null) {
                    // When drawing freehand, save the path and start a new one
                    paths.add(new DrawnPath(currentPath, currentPaint));
                    currentPath = new Path();  // Start a new path
                }
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
        // Reset the bitmap to null or create a blank bitmap if needed
        bitmapToRedraw = null;  // this clears all the before edits
        // Clear all the saved paths
        paths.clear();

        // Clear the current path
        currentPath.reset();

        // Request to redraw the view
        invalidate();
    }

    public void addShape(String shapeType, float x, float y, float size) {
        shapes.add(new DrawnShape(shapeType, x, y, size, currentPaint));
        invalidate();  // Redraw canvas with new shape
    }

    private class DrawnShape {
        Path path;
        Paint paint;
        String shapeType;
        float x, y;  // Position for the shape
        float size;  // Size for the shape

        DrawnShape(String shapeType, float x, float y, float size, Paint paint) {
            this.shapeType = shapeType;
            this.x = x;
            this.y = y;
            this.size = size;
            this.paint = new Paint(paint);  // Copy the paint
            this.path = createShapePath(shapeType, x, y, size);  // Initialize path
        }

        // Check if the touch point is within the shape
        boolean contains(float touchX, float touchY) {
            switch (shapeType) {
                case "circle":
                    float dx = touchX - x;
                    float dy = touchY - y;
                    return (dx * dx + dy * dy) <= (size * size);  // Circle collision detection
                case "rectangle":
                    return touchX >= x - size / 2 && touchX <= x + size / 2 &&
                            touchY >= y - size / 2 && touchY <= y + size / 2;  // Rect collision detection
                default:
                    return false;
            }
        }

        // Update shape's position
        void move(float deltaX, float deltaY) {
            this.x += deltaX;
            this.y += deltaY;
            this.path = createShapePath(shapeType, x, y, size);  // Rebuild the path
        }

        // Resize shape
        void resize(float newSize) {
            this.size = newSize;
            this.path = createShapePath(shapeType, x, y, size);  // Rebuild the path
        }

        // Create the path for the shape
        private Path createShapePath(String shapeType, float x, float y, float size) {
            Path path = new Path();
            switch (shapeType) {
                case "circle":
                    path.addCircle(x, y, size, Path.Direction.CW);
                    break;
                case "rectangle":
                    path.addRect(x - size / 2, y - size / 2, x + size / 2, y + size / 2, Path.Direction.CW);
                    break;
            }
            return path;
        }

    }
}

