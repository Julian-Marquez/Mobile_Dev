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
    private List<Object> allpaths = new ArrayList<>(); //this helps us keep track of both objects
    private List<DrawnPath> paths = new ArrayList<>();
    private List<DrawnShape> shapes = new ArrayList<>(); // Keeps track of all paths and their colors
    private String title;
    private int originalWidth;
    private int originalHeight;
    private float currentShapeSize = 100; // Default size for new shapes
    private DrawnShape currentShape;
    private DrawnPath newpath;
    private List<Object> removeditems = new ArrayList<>(); // keep track of removed items to re add
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

    public List<Object> getPaths(){
        return this.allpaths;
    }

    public void reAddMostRecent() {
        Object lastremoved = null;
        try {
             lastremoved = removeditems.get(removeditems.size() - 1);
        }catch(IndexOutOfBoundsException e){

        }
        if (lastremoved == null || allpaths.contains(lastremoved)) {
            return; // Exit if there is nothing to re-add or if it's already in the list
        }

        // Check the type of lastremoved and add it back to the corresponding list
        if (lastremoved instanceof DrawingView.DrawnPath) {
            // Re-add to the paths list
            paths.add((DrawingView.DrawnPath) lastremoved);
        } else if (lastremoved instanceof DrawnShape) {
            // Re-add to the shapes list
            shapes.add((DrawnShape) lastremoved);
        }

        // Add it back to allpaths to maintain order
        removeditems.remove(lastremoved); // now that it is removed don't store it
        allpaths.add(lastremoved); // restore the last removed

        invalidate();
    }


    public void removeMostRecent() {
        if (allpaths.isEmpty()) {
            return; // Exit if there is nothing to remove
        }

        // Get the most recent object (either a path or shape)
        Object type = allpaths.get(allpaths.size() - 1);

        // Remove it from the paths or shapes list
        if (type instanceof DrawingView.DrawnPath && !paths.isEmpty()) {
            paths.remove(paths.size() - 1); // Remove the most recent path
        } if (shapes.contains(type) && !shapes.isEmpty()) {
            shapes.remove(shapes.size() - 1); // Remove the most recent shape
        }

        // Store the last removed item for re-adding later
        removeditems.add(type);

        // Remove from allpaths as well
        allpaths.remove(allpaths.size() - 1);

        invalidate();
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

    private DrawnShape selectedShape = null;  // Keep track of the selected shape
    private float lastTouchX, lastTouchY;     // Track the last touch coordinates

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Check if the touch point is inside an existing shape
                selectedShape = getTouchedShape(x, y);
                if (selectedShape != null) {
                    lastTouchX = x;
                    lastTouchY = y;
                    return true;  // Stop further event handling as we are moving a shape
                }

                // If no shape is touched, check if we are in shape-drawing mode
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
                if (selectedShape != null) {
                    // Move the selected shape based on touch movement
                    float deltaX = x - lastTouchX;
                    float deltaY = y - lastTouchY;
                    selectedShape.move(deltaX, deltaY);
                    lastTouchX = x;
                    lastTouchY = y;
                    invalidate();  // Request to redraw the view
                } else if (currentShapeType == null) {
                    // Continue freehand drawing
                    currentPath.lineTo(x, y);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (selectedShape != null) {

                } else if (currentShapeType == null) {
                    // When drawing freehand, save the path and start a new one
                    newpath = new DrawnPath(currentPath,currentPaint);
                    allpaths.add(newpath);
                    paths.add(newpath);
                    currentPath = new Path();  // Start a new path
                }
                break;
        }

        // Request to redraw the view
        invalidate();
        return true;
    }

    public boolean isSelectedShape(){
        return selectedShape != null;
    }

    public float getCurrentShapeSize(){
        return currentShapeSize;
    }

    public void setCurrentShapeSize(float newSize) {
        if (selectedShape != null) {
            selectedShape.resize(newSize);
            invalidate();  // Redraw the view with the resized shape
        }
    }

    // Helper method to find a shape that is touched
    private DrawnShape getTouchedShape(float x, float y) {
        for (DrawnShape shape : shapes) {
            if (shape.contains(x, y)) {
                return shape;
            }
        }
        return null;  // No shape is touched
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
        bitmapToRedraw = null;  // this clears all the before edits
        // Clear all the saved paths
        paths.clear();
        shapes.clear();
        allpaths.clear();
        // Clear the current path
        currentPath.reset();

        // Request to redraw the view
        invalidate();
    }

    public void addShape(String shapeType, float x, float y, float size) {
       currentShape = new DrawnShape(shapeType, x, y, size, currentPaint);
        allpaths.add(currentShape);
        shapes.add(currentShape);
        invalidate();  // Redraw canvas with new shape
    }

    private class DrawnShape{
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
        // Check if the touch point is within the shape
        boolean contains(float touchX, float touchY) {
            switch (shapeType) {
                case "circle":
                    float dx = touchX - x;
                    float dy = touchY - y;
                    return (dx * dx + dy * dy) <= (size * size);
                case "rectangle":
                    return touchX >= x - size / 2 && touchX <= x + size / 2 &&
                            touchY >= y - size / 2 && touchY <= y + size / 2;
                case "triangle":
                    // Coordinates of the three vertices of the triangle
                    float x1 = x - size / 2;
                    float y1 = y + size / 2;
                    float x2 = x + size / 2;
                    float y2 = y + size / 2;
                    float x3 = x;
                    float y3 = y - size / 2;

                    float areaOrig = Math.abs((x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1));
                    float area1 = Math.abs((x1 - touchX) * (y2 - touchY) - (x2 - touchX) * (y1 - touchY));
                    float area2 = Math.abs((x2 - touchX) * (y3 - touchY) - (x3 - touchX) * (y2 - touchY));
                    float area3 = Math.abs((x3 - touchX) * (y1 - touchY) - (x1 - touchX) * (y3 - touchY));

                    // Check if the sum equals the original triangle area
                    return (area1 + area2 + area3) == areaOrig;

                case "star":
                    float dxStar = touchX - x;
                    float dyStar = touchY - y;
                    return (dxStar * dxStar + dyStar * dyStar) <= (size * size);


                default:
                    return false;
            }
        }


        void move(float deltaX, float deltaY) {
            this.x += deltaX;
            this.y += deltaY;
            this.path = createShapePath(shapeType, x, y, size);  // Rebuild the path at the new position
        }


        public void resize(float newSize) {
            this.size = newSize;
            // Rebuild the path with the new size
            this.path = createShapePath(shapeType, x, y, size);
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
                case "star":
                    createStarPath(path, x, y, size);
                    break;
                case "triangle":
                    createTrianglePath(path, x, y, size);
                    break;
            }
            return path;
        }

        private void createStarPath(Path path, float cx, float cy, float size) {
            int numPoints = 5;
            double angle = 2 * Math.PI / numPoints;
            float innerRadius = size / 2.5f;

            path.moveTo(
                    (float) (cx + size * Math.cos(0)),
                    (float) (cy + size * Math.sin(0))
            );

            for (int i = 0; i < numPoints; i++) {
                // Outer point
                path.lineTo(
                        (float) (cx + size * Math.cos(i * angle)),
                        (float) (cy + size * Math.sin(i * angle))
                );
                // Inner point
                path.lineTo(
                        (float) (cx + innerRadius * Math.cos(i * angle + angle / 2)),
                        (float) (cy + innerRadius * Math.sin(i * angle + angle / 2))
                );
            }
            path.close();
        }

        private void createTrianglePath(Path path, float cx, float cy, float size) {
            float halfSize = size / 2;
            float height = (float) (Math.sqrt(3) * halfSize);

            // Move to top point of the triangle
            path.moveTo(cx, cy - height / 2);

            // Draw the triangle
            path.lineTo(cx - halfSize, cy + height / 2);
            path.lineTo(cx + halfSize, cy + height / 2);
            path.close();
        }


    }
}

