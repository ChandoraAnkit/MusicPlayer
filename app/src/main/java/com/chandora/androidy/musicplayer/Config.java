package com.chandora.androidy.musicplayer;


import android.net.Uri;

public class Config {

    public static Uri ALBUMN_ART =  Uri.parse("content://media/external/audio/albumart");
    public static final String BROADCAST_PLAY_NEW_AUDIO = "com.chandora.androidy.musicplayer.PlayNewAudio";
    public static final String ACTION_PLAY = "com.chandora.androidy.musicplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.chandora.androidy.musicplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.chandora.androidy.musicplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.chandora.androidy.musicplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "com.chandora.androidy.musicplayer.ACTION_STOP";
    private final String STORAGE  ="com.chandora.androidy.musicplayer.Storage";


}
