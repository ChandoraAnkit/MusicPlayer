package com.chandora.androidy.musicplayer;



public class Audio {

    private String data;
    private String title;
    private String albumn;
    private String artist;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbumn() {
        return albumn;
    }

    public void setAlbumn(String albumn) {
        this.albumn = albumn;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
