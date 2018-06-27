package com.chandora.androidy.musicplayer;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.Manifest.permission_group.STORAGE;

public class StorageUtil {

    private SharedPreferences sharedPreferences;
    private Context context;

    public StorageUtil(Context context){
        this.context =context;
    }

    public void storeAudio(ArrayList<Audio> audioList){

     sharedPreferences = context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
     SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(audioList);
        editor.putString("AudioList",json);
        editor.apply();
    }

    public ArrayList<Audio> loadAudio(){
        sharedPreferences = context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("AudioList",null);

        Type type = new TypeToken<ArrayList<Audio>>(){}.getType();
        return gson.fromJson(json,type);
    }
    public void storeAudioIndex(int index){

        sharedPreferences = context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("AudioIndex",index);
        editor.apply();

    }
    public int loadAudioIndex(){

        sharedPreferences = context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        return sharedPreferences.getInt("AudioIndex",-1);

    }
    public void clearCachedList(){
        sharedPreferences = context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

}
