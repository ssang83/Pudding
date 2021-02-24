package com.enliple.pudding.adapter;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.enliple.pudding.R;
import com.enliple.pudding.model.PermissionObject;

import java.util.ArrayList;

public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.PermissionItemViewHolder> {

    private Context context;
    private ArrayList<PermissionObject.Objects> items;

    public PermissionAdapter(Context context, ArrayList<PermissionObject.Objects> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public PermissionAdapter.PermissionItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.permission_item, parent, false);
        return new PermissionItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PermissionItemViewHolder holder, final int position) {
        PermissionObject.Objects item = items.get(position);
        holder.image.setBackgroundResource(item.getImage());
        holder.title.setText(item.getTitle());
        holder.content.setText(item.getContent());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class PermissionItemViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView image;
        public AppCompatTextView title, content;
        public PermissionItemViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
        }
    }
}
