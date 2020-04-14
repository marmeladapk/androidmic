package com.alimgokkaya.androidmic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.support.v4.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private EditText mEditIp;
    private RecordThread recordThread;
    private int sampleRate;
    private int bufferSize;
    private TextView mTextStatus;
    private View mBtnStart;
    private View mBtnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTextStatus = (TextView) findViewById(R.id.tv_status);
        mEditIp = (EditText) findViewById(R.id.editText);
        mBtnStart = findViewById(R.id.btn_start);
        mBtnStop = findViewById(R.id.btn_stop);
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = mEditIp.getText().toString();
                startService(sampleRate, bufferSize, ip);
            }
        });
        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService();
            }
        });

        int[] srinfo = RecordThread.findSampleRate();
        sampleRate = srinfo[0];
        bufferSize = srinfo[1];
        Log.d("Main", "Recording "+sampleRate+" "+bufferSize);
        showRecording(false);
        mTextStatus.setText("Ready for streaming");
    }

    private void showRecording(boolean recording) {
        if(recording) {
            mTextStatus.setText("Streaming");
            mBtnStop.setVisibility(View.VISIBLE);
            mBtnStart.setVisibility(View.GONE);
            mEditIp.setEnabled(false);
        } else {
            mTextStatus.setText("Stopped. Ready for streaming");
            mBtnStop.setVisibility(View.GONE);
            mBtnStart.setVisibility(View.VISIBLE);
            mEditIp.setEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startService(int sampleRate, int bufferSize, String ip) {
        Intent serviceIntent = new Intent(this, RecordService.class);
        serviceIntent.putExtra("sampleRate", sampleRate);
        serviceIntent.putExtra("bufferSize", bufferSize);
        serviceIntent.putExtra("ip", ip);
        ContextCompat.startForegroundService(this, serviceIntent);
        showRecording(true);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, RecordService.class);
        stopService(serviceIntent);
        showRecording(false);
    }
}
