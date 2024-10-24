package com.example.sketch;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.google.android.material.shape.ShapePath;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.HashSet;


public class DrawingView extends View {
    private int canvasid;
    private int scaledWidth;
    private int scaledHeight;
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
    private boolean eraserMode = false;
    private List<Object> removeditems = new ArrayList<>(); // keep track of removed items to re add
    private String currentShapeType; // Default size for new shapes
    private Bitmap bitmap;
    private boolean isFillMode;
    private List<Path> fillpaths = new ArrayList<>();

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

         this.bitmap = Bitmap.createBitmap(this.scaledWidth, this.scaledHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        draw(canvas);
        return this.bitmap;
    }

    public void setCanvasScale(int width,int height){
        this.scaledWidth = width;
        this.scaledHeight = height;
    }

    public int getScaledWidth(){return scaledWidth;};

    public int getScaledHeight(){return scaledHeight;};

    public Bitmap ScaledCanvas(int width, int height){

        this.bitmap = Bitmap.createBitmap((int) (width * 2), (int) (height * 2), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Calculate scaling factors to fit the entire view in the thumbnail
        float scaleX = (float) width / getScaledWidth();
        float scaleY = (float) height / getScaledHeight();
        float scale = Math.min(scaleX, scaleY); // Scale to fit the thumbnail size

        // Scale the canvas
        canvas.scale((float) (scaleX * 1.5), scaleY * 2);
        //canvas.scale(scale, scale);
        // Translate the canvas to center the view
        canvas.translate((width - getScaledWidth() * scale), (height - getScaledHeight() * scale));

        // Draw the view on the canvas
        draw(canvas);

        return bitmap;

    }


    public Bitmap captureThumbnail(int width, int height) {

        Bitmap bitmap = Bitmap.createBitmap((int) (width * 2), (int) (height * 2), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Calculate scaling factors to fit the entire view in the thumbnail
        float scaleX = (float) width / getScaledWidth();
        float scaleY = (float) height / getScaledHeight();
        float scale = Math.min(scaleX, scaleY); // Scale to fit the thumbnail size

        // Scale the canvas
        canvas.scale((float) (scaleX * 1.5), scaleY * 2);
        //canvas.scale(scale, scale);
        // Translate the canvas to center the view
        canvas.translate((width - getScaledWidth() * scale), (height - getScaledHeight() * scale));

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
        for (DrawnShape ds : shapes) {
            canvas.drawPath(ds.path, ds.paint);
        }
        if(isFillMode) {
            for (int i = 0; i < fillpaths.size(); i++) {
                canvas.drawPath(fillpaths.get(i), currentPaint);
            }
        }

        // Draw the current path
        canvas.drawPath(currentPath, currentPaint);
    }

    private void eraseIfTouched(float x, float y) {
        // Check if the touch point is within any drawn shapes
        DrawnShape shapeToErase = getTouchedShape(x, y);
        if (shapeToErase != null) {
            shapes.remove(shapeToErase);
            allpaths.remove(shapeToErase);
            invalidate();  // Redraw the canvas after removing the shape
        }

    }

    public void enableEraserMode(){
        if(eraserMode) {
            eraserMode = false;
        }
        else{
            eraserMode = true;
        }
    }

    public void floodFill(int x, int y, int targetColor, int replacementColor) {
        if (targetColor == replacementColor) {
            return; // Target color is already the replacement color
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        HashSet<Point> visited = new HashSet<>();

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(x, y));
        Path fillpath = new Path();
        fillpath.moveTo(x, y);

        while (!queue.isEmpty()) {
            Point point = queue.poll();
            int px = point.x;
            int py = point.y;

            if (px < 0 || py < 0 || px >= width || py >= height || visited.contains(point)) {
                continue; // Skip out-of-bounds or visited points
            }

            int currentColor = bitmap.getPixel(px, py);

            if (currentColor == targetColor) {
                bitmap.setPixel(px, py, replacementColor);
                visited.add(point);
                fillpath.lineTo(px, py);

                // Add neighboring points to queue
                queue.add(new Point(px + 1, py));
                queue.add(new Point(px - 1, py));
                queue.add(new Point(px, py + 1));
                queue.add(new Point(px, py - 1));
            }
        }

        currentPath.addPath(fillpath);
        invalidate(); // Redraw the view to show the filling
    }





    public void mirrorCanvas(boolean horizontal) {
        Matrix mirrorMatrix = new Matrix();

        // Horizontal mirroring flip across Y-axis
        if (horizontal) {
            mirrorMatrix.setScale(-1, 1, getWidth() / 2, getHeight() / 2);  // Flip across Y-axis
        } else {
            // Vertical mirroring (flip across X-axis)
            mirrorMatrix.setScale(1, -1, getWidth() / 2, getHeight() / 2);  // Flip across X-axis
        }

        for (DrawnPath path : paths) {
            path.path.transform(mirrorMatrix);
        }

        for (DrawnShape shape : shapes) {
            shape.path.transform(mirrorMatrix);
        }

        // Redraw the canvas with mirrored content
        invalidate();
    }

    private int getPixelColor(int x, int y) {
        return bitmap.getPixel(x, y);
    }

    public void setFillMode(boolean fillMode){
        isFillMode = fillMode;
    }

    private DrawnShape selectedShape = null;  // Keep track of the selected shape
    private float lastTouchX, lastTouchY;     // Track the last touch coordinates

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath = new Path();  // Create a new Path object
                currentPath.moveTo(x, y);
                if (eraserMode) {
                    // In eraser mode, check if a shape or path is touched, and remove it

                    eraseIfTouched(x, y);
                    return true;  // Stop further event handling as we are erasing
                }
                if (isFillMode) {
                    int pixelX = (int) x;
                    int pixelY = (int) y;
                    int targetColor = bitmap.getPixel(pixelX, pixelY);
                    int fillColor = currentPaint.getColor();
                    floodFill(pixelX, pixelY, targetColor, fillColor);
                    invalidate();  // Redraw the view to show the filling
                    return true;  // Return after filling to stop further handling
                }

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
                    return true;
                } else {
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

    public void setCanvasId(int id){
        this.canvasid = id;
    }

    public int getCanvasid(){
        return this.canvasid;
    }

    private class DrawnShape{
        Path path;
        Paint paint;
        String shapeType;
        private int shapeid;
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

        public void setShapeId(int id){
            this.shapeid = id;
        }
        public int getShapeid(){
            return this.shapeid;
        }

    }
}

