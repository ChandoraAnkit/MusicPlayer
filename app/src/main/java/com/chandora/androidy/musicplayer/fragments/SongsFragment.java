package com.chandora.androidy.musicplayer.fragments;


import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chandora.androidy.musicplayer.Audio;
import com.chandora.androidy.musicplayer.Config;
import com.chandora.androidy.musicplayer.MainActivity;
import com.chandora.androidy.musicplayer.MediaPlayerService;
import com.chandora.androidy.musicplayer.R;
import com.chandora.androidy.musicplayer.StorageUtil;
import com.chandora.androidy.musicplayer.adapters.SongsAdapter;
import com.github.florent37.materialviewpager.header.MaterialViewPagerHeaderDecorator;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SongsFragment extends Fragment implements SongsAdapter.ClickListener {

    private RecyclerView mRecyclerView;
    private SongsAdapter mAdapter;
    private ArrayList<Audio> audioList;
    private MainActivity mainActivity;
//    private MediaPlayerService playerService;
//    private boolean serviceBound = false;

    public SongsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        return inflater.inflate(R.layout.fragment_songs, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView = getActivity().findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        loadAudio();
        mAdapter = new SongsAdapter(audioList,this,getActivity());

        mRecyclerView.addItemDecoration(new MaterialViewPagerHeaderDecorator());
        mRecyclerView.setAdapter(mAdapter);

    }

//    private ServiceConnection connection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
//            playerService = binder.getService();
//            serviceBound = true;
//
//            Toast.makeText(getActivity(), "Service Bound", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            serviceBound = false;
//        }
//    };


    private void loadAudio() {

        ContentResolver resolver = getActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] cols = {MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE
                ,MediaStore.Audio.Albums.ALBUM_ID,MediaStore.Audio.Media.ARTIST
                ,MediaStore.Audio.Media.ALBUM,MediaStore.Audio.Media.DATA,};

        String selection =  MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder =  MediaStore.Audio.Media.TITLE  + " ASC ";
        Cursor cursor = resolver.query(uri ,cols, selection, null, sortOrder);


        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();

            while (cursor.moveToNext()) {

                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String albumn = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID));

                Log.i("DETAILS", "loadAudio: id "+id+" data "+data);

                Audio audio = new Audio();
                audio.setAlbumn(albumn);
                audio.setData(data);
                audio.setArtist(artist);
                audio.setTitle(title);
                audio.setId(id);

                audioList.add(audio);
            }
        }

        Log.i("SIZE", "loadAudio: "+audioList.size());
        cursor.close();
    }

    @Override
    public void itemClicked(int pos, View view) {
        playAudio(pos);
    }

    private void playAudio(int audioIndex) {

        Log.i("INDEX", "playAudio: "+audioIndex);

        if (!mainActivity.serviceBound) {
            StorageUtil storage = new StorageUtil(getActivity().getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(getActivity(), MediaPlayerService.class);

            getActivity().startService(playerIntent);
            getActivity().bindService(playerIntent, mainActivity.connection, Context.BIND_AUTO_CREATE);

        }else{

            StorageUtil storage = new StorageUtil(getActivity().getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            Intent broadcastIntent = new Intent(Config.BROADCAST_PLAY_NEW_AUDIO);
            getActivity().sendBroadcast(broadcastIntent);
        }
    }



}
