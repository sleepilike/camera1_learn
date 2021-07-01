package com.example.testone.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.testone.R;

import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private List<String> photoLists;
    private OnItemClickListener onItemClickListener;

    public PhotosAdapter(List<String> photoLists){
        this.photoLists = photoLists;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        //绑定监听事件
        view.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        //设置Image图片
        Glide.with(holder.itemView.getContext()).load(photoLists.get(position))
                .apply(RequestOptions.bitmapTransform(new CircleCrop())
                        .override(((ViewHolder)holder).imageView.getWidth(), ((ViewHolder)holder).imageView.getHeight())
                        .error(R.drawable.default_person_icon))
                .into(((ViewHolder)holder).imageView);
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return photoLists.size();
    }

    @Override
    public void onClick(View v) {
        if(photoLists != null){
            onItemClickListener.onItemClick(v,photoLists.get((int)v.getTag()));

        }
    }

    public interface OnItemClickListener{
        void onItemClick(View v,String path);
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_photo);
        }

    }

}
