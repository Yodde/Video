package com.example.szymek.video;

import android.app.Activity;
import android.app.Application;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StreamVideo extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    Button streamButton;
    boolean isStreaming;
    boolean isPreviewing;

    private int frameWidth = 360;
    private int frameHeight = 280;
    private int previewWidth;
    private int previewHeight;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surface;
    long startTime = 0;
    private final int imageChannels = 2;
    private final int audioChannels = 1;
    private final String format = "flv";
    private int audioSampleRate;
    private int vb = 500000;
    private int ab = 8000;
    private int ahz = 44100;
    //server
    private final int port = 1935;//standardowy port rtmp
    private final String protocol = "rtmp://";
    private final String applicatioonName = "live";//nazwa aplikacji
    private final String streamName = "camera"; //nazwa streamu
    private final String serverAddress = "10.0.78.101";//adres IP servera

    //audio
    private AudioRecord audioRecord;
    private AudioRecordRunnable audioRecordRunnable;
    private Thread audioThread;
    private boolean streamAudio = true;

    //video
    private Camera camera;
    private FFmpegFrameRecorder recorder;
    private Frame frame;
    private int FPS = 15;    //ile fps będziemy w stanie płynnie przesyłać?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_video);
        surface = (SurfaceView) findViewById(R.id.videoSurface);
        surfaceHolder = surface.getHolder();
        surfaceHolder.addCallback(this);
        isStreaming = false;

        previewWidth = getResources().getDisplayMetrics().widthPixels;
        previewHeight = getResources().getDisplayMetrics().heightPixels;
//        isPrepared = false;
//        isInitialized = false;
        streamButton = (Button) findViewById(R.id.recordButton);
        streamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isStreaming) {
                    PrepareRecorder();
                    StartStreaming();
                } else
                    StopStreaming();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isStreaming) {
            try {
                recorder.stop();
                isStreaming = false;
                recorder.release();
            } catch (FFmpegFrameRecorder.Exception e) {
                e.printStackTrace();
            }
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    private void StopStreaming() {
        streamButton.setBackgroundResource(R.mipmap.record);
        if (isStreaming) {
            streamAudio = false;
            try{
                if(audioThread!= null)
                    audioThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            audioThread = null;
            audioRecordRunnable = null;

            try {
                recorder.stop();
                recorder.release();
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
            isStreaming = false;
        }
    }

    private void StartStreaming() {
        try {
            Log.v("STREAMING", "Prepare to streaming");
            streamButton.setBackgroundResource(R.mipmap.stop_recording);
            recorder.start();
            startTime = System.currentTimeMillis();
            isStreaming = true;
            audioThread.start();
        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
        }
        Log.v("STREAMING", "Stream ok");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        camera = Camera.open();
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(camera!=null) {
            camera.stopPreview();
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            //Collections.sort(sizes)
            for (int i = 0; i < sizes.size(); i++) {
                Camera.Size size = sizes.get(i);
                if (size.width >= frameWidth && size.height >= frameHeight || i == sizes.size() - 1) {
                    frameWidth = size.width;
                    frameHeight = size.height;
                    break;
                }
            }
            parameters.setPreviewSize(frameWidth, frameHeight);
            setSurfaceSize(frameWidth, frameHeight);
            parameters.setPreviewFrameRate(FPS);

            try {
                camera.setParameters(parameters);
                camera.setPreviewDisplay(surfaceHolder);
                camera.setPreviewCallback(StreamVideo.this);
                camera.startPreview();
            } catch (IOException e) {
                Log.e("SURFACE", "Surface not changed");
                e.printStackTrace();
            }
        }
    }

    private void setSurfaceSize(int width, int height) {
        surface.getLayoutParams().width = (int) (previewWidth * ((float) width / (float) height));
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    //Pobieranie ramek z kamery i przekazywanie ich do FFMPEG
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (audioRecord == null || audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            startTime = System.currentTimeMillis();
            return;
        }
        if (frame != null && isStreaming) {
            ((ByteBuffer) frame.image[0].position(0)).put(data);
            try {
                long time = 1000 * (System.currentTimeMillis() - startTime);
                if (time > recorder.getTimestamp())
                    recorder.setTimestamp(time);
                recorder.record(frame);
            } catch (FFmpegFrameRecorder.Exception e) {
                Log.e("FFMPEG error", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String BuildStreamEndpoint() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(protocol);
        stringBuilder.append(serverAddress)
                .append(":")
                .append(port)
                .append("/")
                .append(applicatioonName)
                .append("/").append(streamName);
        return stringBuilder.toString();
    }

    private void PrepareRecorder() {
        Log.v("FFMPEG", "Prepare FFMPEG");
        //CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        // frameWidth = profile.videoFrameWidth;
        // frameHeight = profile.videoFrameHeight;

        audioSampleRate = ahz;//profile.audioSampleRate;
        if (frame == null) {
            frame = new Frame(frameWidth, frameHeight, Frame.DEPTH_BYTE, imageChannels);
        }
        recorder = new FFmpegFrameRecorder(BuildStreamEndpoint(), frameWidth, frameHeight, audioChannels);
        recorder.setFormat(format);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setVideoBitrate(vb);
        recorder.setAudioBitrate(ab);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setSampleRate(audioSampleRate);
        recorder.setFrameNumber(FPS);

        audioRecordRunnable = new AudioRecordRunnable();
        audioThread = new Thread(audioRecordRunnable);
        streamAudio = true;
        isStreaming = true;
        Log.v("FFMPEG", "Recorder prepared");
    }

    class AudioRecordRunnable implements Runnable {

        @Override
        public void run() {
            Log.i("AUDIO", "IT's audio thread");
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

            int bufferSize;
            ShortBuffer audioData;
            int bufferReadResult;

            bufferSize = AudioRecord.getMinBufferSize(audioSampleRate,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, audioSampleRate,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            audioData = ShortBuffer.allocate(bufferSize);
            audioRecord.startRecording();

            while (streamAudio) {
                bufferReadResult = audioRecord.read(audioData.array(), 0, audioData.capacity());
                audioData.limit(bufferReadResult);
                Log.i("AUDIO", "buffer read result" + bufferReadResult);
                if (bufferReadResult > 0 && isStreaming) {
                    try {
                        recorder.recordSamples(audioData);
                    } catch (FFmpegFrameRecorder.Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
                Log.v("AUDIO", "audioRecord released");
            }
        }
    }
}
