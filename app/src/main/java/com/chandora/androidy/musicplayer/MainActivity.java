package com.chandora.androidy.musicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chandora.androidy.musicplayer.adapters.PagerSegmentAdapter;
import com.chandora.androidy.musicplayer.fragments.BottomSheetSongFragment;
import com.chandora.androidy.musicplayer.fragments.SongsFragment;
import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.header.HeaderDesign;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public  MediaPlayerService playerService;
    public  boolean serviceBound = false;
//    private ArrayList<Audio> audioList;
//    private RecyclerView mRecyclerView;
//    private SongsAdapter mAdapter;
    private MaterialViewPager mViewPager;
    private PagerSegmentAdapter mPagerAdapter;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private RelativeLayout llBottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private BottomSheetDialogFragment mDialogFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkPermission()){
            requestPermission();
        }
        mDialogFragment = new BottomSheetSongFragment();
        mDialogFragment.show(getSupportFragmentManager(),mDialogFragment.getTag());


//        llBottomSheet =findViewById(R.id.bottom_sheet);
//
//        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
//        if (bottomSheetBehavior != null){
//            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//                @Override
//                public void onStateChanged(@NonNull View bottomSheet, int newState) {
//
//                    switch (newState){
//
//                        case BottomSheetBehavior.STATE_HIDDEN:
//                            break;
//                        case BottomSheetBehavior.STATE_EXPANDED:
//                            break;
//                        case BottomSheetBehavior.STATE_COLLAPSED:
//                            break;
//                        case BottomSheetBehavior.STATE_DRAGGING:
//                            break;
//                        case BottomSheetBehavior.STATE_SETTLING:
//                            break;
//                    }
//                }
//
//                @Override
//                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//
//                }
//            });
//        }

//
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
////
////
//
//        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                Toast.makeText(MainActivity.this, ""+newState, Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                Toast.makeText(MainActivity.this, "On Slide" +
//                        "", Toast.LENGTH_SHORT).show();
//            }
//        });

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this,mDrawerLayout,0,0);
        mDrawerLayout.setDrawerListener(mToggle);


        mViewPager = findViewById(R.id.materialViewPager);
        mPagerAdapter = new PagerSegmentAdapter(getSupportFragmentManager());
        ViewPager pager = mViewPager.getViewPager();
        pager.setAdapter(mPagerAdapter);

      Toolbar toolbar = mViewPager.getToolbar();
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle("");
            actionBar.setHomeAsUpIndicator(R.drawable.hand);
        }

//        mRecyclerView = findViewById(R.id.recycler_view);
//        loadAudio();

//        mAdapter = new SongsAdapter(audioList,this,MainActivity.this);
//        mRecyclerView.setHasFixedSize(true);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mRecyclerView.setAdapter(mAdapter);

        mViewPager.setMaterialViewPagerListener(new MaterialViewPager.Listener() {
            @Override
            public HeaderDesign getHeaderDesign(int page) {
                switch (page) {
                    case 0:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.green,
                                "http://phandroid.s3.amazonaws.com/wp-content/uploads/2014/06/android_google_moutain_google_now_1920x1080_wallpaper_Wallpaper-HD_2560x1600_www.paperhi.com_-640x400.jpg");
                    case 1:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.blue,
                                "http://www.hdiphonewallpapers.us/phone-wallpapers/540x960-1/540x960-mobile-wallpapers-hd-2218x5ox3.jpg");
                    case 2:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.cyan,
                                "http://www.droid-life.com/wp-content/uploads/2014/10/lollipop-wallpapers10.jpg");
                    case 3:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.red,
                                "http://www.tothemobile.com/wp-content/uploads/2014/07/original.jpg");
                }

                //execute others actions if needed (ex : modify your header logo)

                return null;
            }
        });

        pager.setOffscreenPageLimit(pager.getAdapter().getCount());
        mViewPager.getPagerTitleStrip().setViewPager(pager);



    }

    public ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            playerService = binder.getService();
            serviceBound = true;

            Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

//    private void playAudio(int audioIndex) {
//
//        Log.i("INDEX", "playAudio: "+audioIndex);
//
//        if (!serviceBound) {
//            StorageUtil storage = new StorageUtil(getApplicationContext());
//            storage.storeAudio(audioList);
//            storage.storeAudioIndex(audioIndex);
//
//            Intent playerIntent = new Intent(this, MediaPlayerService.class);
//
//            startService(playerIntent);
//            bindService(playerIntent, connection, Context.BIND_AUTO_CREATE);
//
//        }else{
//
//            StorageUtil storage = new StorageUtil(getApplicationContext());
//            storage.storeAudioIndex(audioIndex);
//
//            Intent broadcastIntent = new Intent(Config.BROADCAST_PLAY_NEW_AUDIO);
//            sendBroadcast(broadcastIntent);
//        }
//    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
//        outState.putBoolean("SERVICE_STATE", serviceBound);
//        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            serviceBound = savedInstanceState.getBoolean("SERVICE_STATE");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(connection);
            playerService.stopSelf();
        }
    }

//    private void loadAudio() {
//
//        ContentResolver resolver = getContentResolver();
//        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        String[] cols = {MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE
//                ,MediaStore.Audio.Albums.ALBUM_ID,MediaStore.Audio.Media.ARTIST
//                ,MediaStore.Audio.Media.ALBUM,MediaStore.Audio.Media.DATA,};
//
//        String selection =  MediaStore.Audio.Media.IS_MUSIC + "!= 0";
//        String sortOrder =  MediaStore.Audio.Media.TITLE  + " ASC ";
//        Cursor cursor = resolver.query(uri ,cols, selection, null, sortOrder);
//
//
//        if (cursor != null && cursor.getCount() > 0) {
//            audioList = new ArrayList<>();
//
//            while (cursor.moveToNext()) {
//
//                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
//                String albumn = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
//                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
//                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID));
//
//                Log.i("DETAILS", "loadAudio: id "+id+" data "+data);
//
//                Audio audio = new Audio();
//                audio.setAlbumn(albumn);
//                audio.setData(data);
//                audio.setArtist(artist);
//                audio.setTitle(title);
//                audio.setId(id);
//
//                audioList.add(audio);
//            }
//        }
//
//        Log.i("SIZE", "loadAudio: "+audioList.size());
//        cursor.close();
//    }

    private boolean checkPermission(){

        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       if (mToggle.onOptionsItemSelected(item))
           return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mToggle.syncState();
    }


    //    @Override
//    public void itemClicked(int pos, View view) {
//        playAudio(pos);
//    }
}
