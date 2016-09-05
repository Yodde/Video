package com.example.szymek.video;

import android.app.Activity;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlayVideo extends Activity {
    VideoView videoView;
    MediaController mediaController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
    }
    @Override
    protected void onResume(){
        super.onResume();
        videoView = (VideoView) findViewById(R.id.play_video);
        mediaController = new MediaController(this);
        //videoView.stopPlayback();
        PlaySavedVideo();
    }

    public void PlaySavedVideo(){
        Uri uri = Uri.parse(MainActivity.VIDEOPAHT + "/" + MainActivity.VIDEONAME);
        //videoView.setVideoPath(MainActivity.VIDEOPAHT + "/" + MainActivity.VIDEONAME);
        videoView.setVideoURI(uri);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
//        this.getWindow().addFlags(Window.);
        videoView.start();
    }
}
