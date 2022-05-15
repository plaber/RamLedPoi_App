package com.ramledpoi.ledcontroll;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebAppInterface {
    Context mContext;
    private SharedPreferences mSettings;

    /** Instantiate the interface and set the context */
    WebAppInterface(Context c) {
        mContext = c;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        mSettings = mContext.getSharedPreferences("conf", Context.MODE_PRIVATE);
        Toast.makeText(mContext, "select " + toast, Toast.LENGTH_SHORT).show();
        Log.d("WW", toast);
        String ipe = "";
        if (mSettings.contains("ipe")) ipe = mSettings.getString("ipe", "");
        String[] ips = ipe.split("\n");
        String myip = myNet.myIP(mContext);
        if(myip.equals("0.0.0.0") && !ipe.isEmpty()){
            myip = ips[0];
        }
        String[] myipr = myip.split("\\.");
        final String ipbrc = myipr[0] + "." + myipr[1] + "." + myipr[2] + ".255";
        final String qw = "modefile=" + toast;
        new Thread(new Runnable() {
            public void run() {
                myNet.udpSend(ipbrc,qw);
            }
        }).start();
    }
}
