package com.ramledpoi.ledcontroll;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class MyService extends Service {
    final String LOG_TAG = "BKG";

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        someTask();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    void someTask() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    int port = 60202;

                    DatagramSocket dsocket = new DatagramSocket(port);
                    byte[] buffer = new byte[2048];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    while (true) {
                        dsocket.receive(packet);
                        String lText = new String(buffer, 0, packet.getLength());
                        Log.d(LOG_TAG, lText);
                        packet.setLength(buffer.length);
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            v.vibrate(300);
                        }
                    }
                } catch (Exception e) {
                    //System.err.println(e);
                    //e.printStackTrace();
                    Log.d(LOG_TAG, "ERR " + e.getMessage());
                }
            }
        }).start();
    }
}
