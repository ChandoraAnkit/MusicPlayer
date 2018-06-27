package com.chandora.androidy.musicplayer.adapters;


import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.chandora.androidy.musicplayer.fragments.AlbumnsFragment;
import com.chandora.androidy.musicplayer.fragments.PlayListFragment;
import com.chandora.androidy.musicplayer.fragments.SongsFragment;

public class PagerSegmentAdapter extends FragmentPagerAdapter {

    public PagerSegmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
       switch (position % 3){
           case 0:
               return new SongsFragment();
           case 1:
               return new PlayListFragment();
           case 2:
               return new AlbumnsFragment();
           default:
               return new SongsFragment();
       }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position % 3){
            case 0:
                return "Songs";
            case 1:
              return  "Playlist";
            case 2:
                return "Favourites";
                default:
                    return  null;
        }
    }
}
