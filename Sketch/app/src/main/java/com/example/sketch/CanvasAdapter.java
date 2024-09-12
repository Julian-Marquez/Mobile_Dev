package com.example.sketch;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CanvasAdapter extends RecyclerView.Adapter<CanvasAdapter.CanvasViewHolder> {

    private ArrayList<DrawingView> canvasList;
    private OnItemClickListener onItemClickListener;

    public CanvasAdapter(ArrayList<DrawingView> canvasList) {
        this.canvasList = canvasList;
       // this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public CanvasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for grid items
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.canvas_item, parent, false);
        return new CanvasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CanvasViewHolder holder, int position) {
        DrawingView canvas = canvasList.get(position);


        // Capture a thumbnail
        Bitmap thumbnail = canvas.captureThumbnail(400, 400);
        holder.image.setImageBitmap(thumbnail);
        holder.title.setText(canvas.getTitle()); // Assuming you have a method getTitle() in DrawingView

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(canvas);
            }
        });

    }

    @Override
    public int getItemCount() {
        return canvasList.size();
    }

    public static class CanvasViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image; // Use ImageView to display the thumbnail

        public CanvasViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.canvas_title);
            image = itemView.findViewById(R.id.canvas_image); // Update to match your layout file
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DrawingView canvas);
    }
}
