package com.chandora.androidy.musicplayer.adapters;


import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chandora.androidy.musicplayer.Audio;
import com.chandora.androidy.musicplayer.Config;
import com.chandora.androidy.musicplayer.R;

import java.util.ArrayList;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.MyViewHolder> {
    public ArrayList<Audio> audiolist;
    static ClickListener clickListener;
    public Context context;


    public SongsAdapter(ArrayList<Audio> list, ClickListener clickListener, Context context) {
        this.audiolist = list;
        this.clickListener = clickListener;
        this.context = context;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_song, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.audio_title_tv.setText(audiolist.get(position).getTitle());
        Log.i("ADAPTER", "onBindViewHolder: " + audiolist.get(position).getId());
        Uri uri = ContentUris.withAppendedId(Config.ALBUMN_ART, Long.parseLong(audiolist.get(position).getId()));
        Glide.with(context).asBitmap().load(uri).apply(new RequestOptions().placeholder(R.drawable.music_icon).error(R.drawable.music_icon)).into(holder.audio_image_iv);

    }


    @Override
    public int getItemCount() {
        return audiolist.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView audio_title_tv;
        ImageView audio_image_iv;

        public MyViewHolder(View itemView) {
            super(itemView);

            audio_title_tv = itemView.findViewById(R.id.audio_title);
            audio_image_iv = itemView.findViewById(R.id.audio_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            SongsAdapter.clickListener.itemClicked(getAdapterPosition(), v);
        }
    }


    public interface ClickListener {
        void itemClicked(int pos, View view);
    }

}



