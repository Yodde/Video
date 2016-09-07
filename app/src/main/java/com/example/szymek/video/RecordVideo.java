package com.example.szymek.video;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;


/**
 * Created by Szymek on 03.08.2016.
 */
public class RecordVideo extends Activity implements SurfaceHolder.Callback {
    MediaRecorder mediaRecorder ;
    Camera camera;
    SurfaceView surface;
    SurfaceHolder surfaceHolder;
    boolean isRecording;
    boolean isPrepared;
    boolean isInitialized;
    Button recordButton;
    //@BindView(R.id.recordButton)
    //ImageView recordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_video);
        surface = (SurfaceView) findViewById(R.id.videoSurface);
        surfaceHolder = surface.getHolder();
        surfaceHolder.addCallback(this);
        isRecording = false;
        isPrepared = false;
        isInitialized = false;
        recordButton = (Button) findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRecording)
                    PrepareRecording();
                else
                    StopRecording();
            }
        });
    }

    @Override
    protected void onStop(){
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Clear();
    }
    public void PrepareRecording(){
        recordButton.setClickable(false);
        camera.unlock();
        camera.stopPreview();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(camera);
        isInitialized = true;
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mediaRecorder.setOutputFile(MainActivity.VIDEOPAHT + "/" + MainActivity.VIDEONAME);
        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        try {
            mediaRecorder.prepare();
            isPrepared = true;
            StartRecording();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void StartRecording(){
        if(isPrepared) {
            mediaRecorder.start();
            isRecording = true;
            recordButton.setBackgroundResource(R.mipmap.stop_recording);
            recordButton.setClickable(true);
        }
    }
    public void StopRecording(){
        if(isRecording){
            mediaRecorder.stop();
            mediaRecorder.reset();
            camera.startPreview();
            camera.lock();
            recordButton.setBackgroundResource(R.mipmap.record);
            isRecording = false;
        }
    }

    public void Clear(){
        if(isInitialized) {
            if (isRecording) {
                mediaRecorder.stop();
            }
            mediaRecorder.reset();
            mediaRecorder.release();
            isRecording = false;
            isInitialized =false;
            isPrepared = false;
        }
        camera.lock();
        camera.stopPreview();
        surfaceHolder.removeCallback(this);
        camera.release();
        camera = null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
        try {
            camera.setParameters(camera.getParameters());
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
