package com.example.szymek.video;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    public static String VIDEONAME = "CameraVideo.mp4";
    public static String VIDEOPAHT = Environment.getExternalStorageDirectory().getAbsolutePath();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button recordButton = (Button) findViewById(R.id.record);
        recordButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MainActivity.this,RecordVideo.class);
                startActivity(intent);
            }
        });
        final Button playButton = (Button) findViewById(R.id.play);
        playButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MainActivity.this,PlayVideo.class);
                startActivity(intent);
            }
        });
        final Button streamButton = (Button) findViewById(R.id.stream);
        streamButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MainActivity.this,StreamVideo.class);
                startActivity(intent);
            }
        });
    }
}
