package com.alimgokkaya.androidmic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class RecordService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private RecordThread recordThread;
    private static final int SERVER_PORT = 9900;

    public class UdpStreamClient implements RecordingListener {

        private InetAddress hostAddress;
        private DatagramSocket mSocket;
        private int packetIndex = 0;

        public UdpStreamClient(String ip) {
            try {
                hostAddress =  InetAddress.getByName(ip);
                mSocket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onBytes(int freq, byte[] buffer, int numBytes) {
            byte[] message = ByteBuffer.allocate(numBytes + 8)
                    .putInt(freq)
                    .putInt(packetIndex)
                    .put(buffer, 0, numBytes)
                    .array();
            DatagramPacket p = new DatagramPacket(message, message.length, hostAddress, SERVER_PORT);
            try {
                mSocket.send(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
            packetIndex++;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int sampleRate = intent.getIntExtra("sampleRate",0);
        int bufferSize = intent.getIntExtra("bufferSize",0);
        String ip = intent.getStringExtra("ip");

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Foreground Service")
                .setContentText("Microphone input streaming")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(pendingIntent)
                .setChannelId(CHANNEL_ID)
                .build();
        startForeground(1, notification);

        if (recordThread == null) {
            recordThread = new RecordThread(sampleRate, bufferSize, new UdpStreamClient(ip));
            recordThread.start();
        }
        //do heavy work on a background thread
        //stopSelf();
        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        if (recordThread != null) {
            recordThread.finish();
            //byte buffer[] = recordThread.getData();
            //thread.setSoundData(buffer,(int)(200*mRecordRatio),buffer.length/2-1);
            recordThread = null;
        }
        super.onDestroy();

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
//    @TargetApi(Build.VERSION_CODES.M)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_NONE
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}