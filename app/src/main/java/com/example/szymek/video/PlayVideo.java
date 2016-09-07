package com.example.szymek.video;

import android.app.Activity;
import android.media.AudioManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.stream.Stream;

public class PlayVideo extends Activity {
    VideoView videoView;
    MediaController mediaController;
    PLAY_OPTIONS playOptions;
    AudioManager audioManager;
    String hls = "http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8";
    String stream = "http://78.8.9.173:18200";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        playOptions = (PLAY_OPTIONS) getIntent().getSerializableExtra(MainActivity.PLAYOPTIONS);
    }
    @Override
    protected void onResume(){
        super.onResume();
        videoView = (VideoView) findViewById(R.id.play_video);
        mediaController = new MediaController(this);
        if(!isChangingConfigurations()){
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0,0);
            switch (playOptions){
                case PLAY_VIDEO:
                    Play(Uri.parse(MainActivity.VIDEOPAHT+"/"+MainActivity.VIDEONAME));
                    break;
                case PLAY_HLS:
                    Play(Uri.parse(hls));
                    break;
                case PLAY_STREAM:
                    Play(Uri.parse(stream));
                    break;
                default:
                    Play(Uri.parse(MainActivity.VIDEOPAHT+"/"+MainActivity.VIDEONAME));
            }
        }
        //videoView.stopPlayback();

    }

    public void Play(Uri uri){
        videoView.setVideoURI(uri);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.start();
    }
}
