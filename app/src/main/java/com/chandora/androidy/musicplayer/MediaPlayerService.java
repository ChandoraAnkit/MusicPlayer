package com.chandora.androidy.musicplayer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;

import android.net.Uri;
import android.os.Binder;
import android.os.Build;

import android.os.IBinder;

import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import static com.chandora.androidy.musicplayer.Config.ACTION_NEXT;
import static com.chandora.androidy.musicplayer.Config.ACTION_PAUSE;
import static com.chandora.androidy.musicplayer.Config.ACTION_PLAY;
import static com.chandora.androidy.musicplayer.Config.ACTION_PREVIOUS;
import static com.chandora.androidy.musicplayer.Config.ACTION_STOP;


public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {


    private final IBinder binder = new LocalBinder();
    private boolean onGoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private MediaPlayer mediaPlayer;
    private int resumePosition;
    private AudioManager audioManager;
    private ArrayList<Audio> audioList;
    private int audioIndex = -1;
    private Audio activeAudio;

    public static final String TAG = MediaPlayerService.class.getSimpleName();

    //MediaSession

    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls controls;

    private static final int NOTIFICATION_ID = 101;

    private void initMediaSession() {

        if (mediaSession != null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
            mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
            controls = mediaSession.getController().getTransportControls();
            mediaSession.setActive(true);
            mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

            updateMetaData();

            mediaSession.setCallback(new MediaSessionCompat.Callback() {
                @Override
                public void onPlay() {
                    super.onPlay();
                    Log.i("LIFE_CYCLE", "onPlay: ");
                    resumeMedia();
                    buildNotification(PlaybackStatus.PLAYING);
                }

                @Override
                public void onPause() {
                    super.onPause();
                    Log.i("LIFE_CYCLE", "onPause: ");

                    pauseMedia();
                    buildNotification(PlaybackStatus.PAUSED);
                }

                @Override
                public void onSkipToNext() {
                    super.onSkipToNext();
                    Log.i("LIFE_CYCLE", "onSkipToNext: ");

                    skipToNext();
                    updateMetaData();
                    buildNotification(PlaybackStatus.PLAYING);
                }

                @Override
                public void onSkipToPrevious() {
                    super.onSkipToPrevious();
                    skipToPrevious();
                    Log.i("LIFE_CYCLE", "onPrevious: ");
                    updateMetaData();
                    buildNotification(PlaybackStatus.PLAYING);
                }

                @Override
                public void onStop() {
                    super.onStop();
                    removeNotification();
                    Log.i("LIFE_CYCLE", "onStop: ");
                    stopSelf();
                }

                @Override
                public void onSeekTo(long pos) {
                    super.onSeekTo(pos);
                    Log.i("LIFE_CYCLE", "onSeekTo: ");

                }
            });

        } else {

            Toast.makeText(this, "Required higher android version", Toast.LENGTH_SHORT).show();
        }
    }

    private void skipToNext() {
        if (audioIndex == audioList.size() - 1) {
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            activeAudio = audioList.get(++audioIndex);
        }

        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void skipToPrevious() {
        if (audioIndex == 0) {
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
        } else {
            activeAudio = audioList.get(--audioIndex);
        }

        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        mediaPlayer.reset();
        initMediaPlayer();

    }


    private BroadcastReceiver becomeNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
            if (audioIndex != -1 && audioIndex < audioList.size()) {
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    private void updateMetaData() {


        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), android.R.drawable.arrow_up_float);
        mediaSession.setMetadata(new MediaMetadataCompat.Builder().putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getAlbumn())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getTitle()).build());
    }

    private void register_playNewAudio() {

        IntentFilter filter = new IntentFilter(Config.BROADCAST_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }

    private void buildNotification(PlaybackStatus playbackStatus) {

        Log.i(TAG, "buildNotification: " + playbackStatus.name());

        int notificationAction = -1;
        PendingIntent play_pause_action = null;

        if (playbackStatus == PlaybackStatus.PLAYING) {

            notificationAction = android.R.drawable.ic_media_pause;
            Log.i(TAG, "Inside");
            play_pause_action = playbackAction(1);

        } else if (playbackStatus == PlaybackStatus.PAUSED) {

            Log.i(TAG, "Exit");
            notificationAction = android.R.drawable.ic_media_play;
            play_pause_action = playbackAction(0);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setShowWhen(false)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setColor(getResources().getColor(R.color.colorPrimary))
                // Set the large and small icons
                .setLargeIcon(getBitmap(activeAudio.getId()))
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                .setContentText(activeAudio.getArtist())
                .setContentTitle(activeAudio.getAlbumn())
                .setContentInfo(activeAudio.getTitle())
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))

                .addAction(notificationAction, "pause", play_pause_action)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));


        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, builder.build());

    }

    private PendingIntent playbackAction(int actionNumber) {

        Intent playBackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {

            case 0:
                playBackAction.setAction(ACTION_PLAY);
                Log.i(TAG, "playbackAction: " + "PLAY");
                return PendingIntent.getService(this, actionNumber, playBackAction, 0);

            case 1:
                playBackAction.setAction(ACTION_PAUSE);
                Log.i(TAG, "playbackAction: " + "PAUSE");
                return PendingIntent.getService(this, actionNumber, playBackAction, 0);

            case 2:
                playBackAction.setAction(ACTION_NEXT);
                Log.i(TAG, "playbackAction: " + "NEXT");
                return PendingIntent.getService(this, actionNumber, playBackAction, 0);

            case 3:
                playBackAction.setAction(ACTION_PREVIOUS);
                Log.i(TAG, "playbackAction: " + "PREVIOUS");
                return PendingIntent.getService(this, actionNumber, playBackAction, 0);

            default:
                break;
        }

        return null;
    }

    public void registerBecomingNoiseReceiver() {

        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomeNoisyReceiver, filter);
    }

    public void callStateListener() {

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            onGoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mediaPlayer != null) {
                            if (onGoingCall)
                                onGoingCall = false;
                            resumeMedia();
                        }
                        break;
                }

            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        callStateListener();
        registerBecomingNoiseReceiver();
        register_playNewAudio();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {

            StorageUtil storageUtil = new StorageUtil(getApplicationContext());
            audioList = storageUtil.loadAudio();
            audioIndex = storageUtil.loadAudioIndex();

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (requestAudioFocus() == false) {
            stopSelf();
        }
        if (mediaSessionManager == null) {

            try {
                initMediaSession();
                initMediaPlayer();

            } catch (Exception e) {
                e.printStackTrace();
                stopSelf();
            }

            buildNotification(PlaybackStatus.PLAYING);
        }

        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopMedia();
        stopSelf();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:

                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:

                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }

    }

    private boolean requestAudioFocus() {

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        Log.i("TAG", "requestAudioFocus: " + result);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        }
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.i(TAG, "onBufferingUpdate: "+percent);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {

            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public class LocalBinder extends Binder {

        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    private void initMediaPlayer() {

        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnErrorListener(this);

        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(activeAudio.getData());
            Log.i("DATA", "initMediaPlayer: " + activeAudio.getData());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }

        mediaPlayer.prepareAsync();
    }

    private void playMedia() {

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {

        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

    }

    private void resumeMedia() {

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }

    }

    private void pauseMedia() {

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }

        removeAudioFocus();
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        removeNotification();
        unregisterReceiver(playNewAudio);
        unregisterReceiver(becomeNoisyReceiver);
        new StorageUtil(getApplicationContext()).clearCachedList();
    }


    private void removeNotification() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void handleIncomingActions(Intent playBackAction) {
        if (playBackAction == null || playBackAction.getAction() == null)
            return;

        String action = playBackAction.getAction();
        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            controls.play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            controls.pause();
        } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
            controls.skipToNext();
        } else if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            controls.skipToPrevious();
        } else if (action.equalsIgnoreCase(ACTION_STOP)) {
            controls.stop();
        }
    }

    public Bitmap getBitmap(String id) {

        Uri albumnArtUri = ContentUris.withAppendedId(Config.ALBUMN_ART, Long.parseLong(id));

        Bitmap bitmap = null;

        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), albumnArtUri);

        } catch (Exception e) {
            e.printStackTrace();
            bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.music_icon);
        } finally {
            return bitmap;
        }


    }
}
