package com.ramledpoi.ledcontroll;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, controlFragment.OnFragmentInteractionListener, picsFragment.OnFragmentInteractionListener, configFragment.OnFragmentInteractionListener,
        ipFragment.OnFragmentInteractionListener, updateFragment.OnFragmentInteractionListener, shareFragment.OnFragmentInteractionListener, helpFragment.OnFragmentInteractionListener {

    private SharedPreferences mSettings;
    private boolean firstAddIp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Fragment fragment = null;
        Class fragmentClass = null;
        fragmentClass = controlFragment.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        mSettings = getSharedPreferences("conf", Context.MODE_PRIVATE);

        myDatagramReceiver = new MyDatagramReceiver();
        myDatagramReceiver.start();
        Boolean cup = true;
        if (mSettings.contains("cup")) cup = mSettings.getBoolean("cup", true);
        /*
        if (mSettings.contains("ipe")) {
            SharedPreferences.Editor editor = mSettings.edit();
            editor.remove("ipe");
            editor.apply();
        }
        */
        if(cup) new httpGet(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, updateList);
        Log.d("BKG","start bkg");
        startService(new Intent(this, MyService.class));
        int permissionStatus = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permissionStatus != PackageManager.PERMISSION_GRANTED){
            //Toast.makeText(context, "Нет разрешения на чтение данных", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.debug, menu);
        return true;
    }

    final Context context = this;

    public static final String updateLink = "https://cloud.mail.ru/public/f34A/7usuoJjrh";
    public static final String updateList = "https://cloud.mail.ru/api/v2/folder?weblink=f34A%2F7usuoJjrh&sort=%7B\"type\"%3A\"mtime\"%2C\"order\"%3A\"desc\"%7D";

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;
        if (id == R.id.nav_control) {
            getSupportActionBar().setTitle(R.string.menu_control);
            fragment = controlFragment.newInstance("","");
        } else if (id == R.id.nav_pictures) {
            getSupportActionBar().setTitle(R.string.menu_pictures);
            String  ipe = "";
            Boolean cch = false;
            if (mSettings.contains("ipe")) ipe = mSettings.getString("ipe", "");
            if (mSettings.contains("cch")) cch = mSettings.getBoolean("cch", false);
            fragment = picsFragment.newInstance(ipe, cch);
        } else if (id == R.id.nav_config) {
            getSupportActionBar().setTitle(R.string.menu_config);
            fragment = configFragment.newInstance("","");
        } else if (id == R.id.nav_setupip) {
            getSupportActionBar().setTitle(R.string.menu_setupip);
            String  ipe = "";
            if (mSettings.contains("ipe")) ipe = mSettings.getString("ipe", "");
            fragment = ipFragment.newInstance(ipe);
        } else if (id == R.id.nav_update) {
            getSupportActionBar().setTitle(R.string.menu_update);
            Boolean cup = true;
            if (mSettings.contains("cup")) cup = mSettings.getBoolean("cup", true);
            fragment = updateFragment.newInstance(updateLink,cup);
        } else if (id == R.id.nav_feedback) {
            getSupportActionBar().setTitle(R.string.menu_feedb);
            fragment = shareFragment.newInstance("","");
        } else if (id == R.id.nav_help) {
            getSupportActionBar().setTitle(R.string.menu_help);
            fragment = helpFragment.newInstance("","");
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static void hidekb(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View v = activity.getCurrentFocus();
        if(v != null) inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void onButton_CheckUpdClick(View v){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateLink));
        startActivity(browserIntent);
    }

    public void onCheckBox_UpdateClick(View v){
        CheckBox cup = findViewById(R.id.checkBoxUpdate);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean("cup", cup.isChecked());
        editor.apply();
    }

    public void onButton_ShareClick(View v){
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String textToSend = updateLink;
        intent.putExtra(Intent.EXTRA_TEXT, textToSend);
        try {
            startActivity(Intent.createChooser(intent, "Поделиться программой"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_SHORT).show();
        }
    }

    public void onButton_ipSaveClick(View v){
        hidekb(this);
        EditText ipe = findViewById(R.id.editTextIpe);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("ipe", ipe.getText().toString());
        editor.apply();
        Toast.makeText(getBaseContext(), "Сохранено", Toast.LENGTH_SHORT).show();
        if (pah == 5) {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.kto);
            mp.start();
        }
    }

    public void onButton_ApGetIp(View v){
        myNet.getClientList(this);
        if(pah==5) {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.aaa);
            mp.start();
        }
    }

    public void onButton_UdpGetIpClick(View v){
        final String ipbrc = myNet.brIP(getBaseContext());
        EditText ips = ((Activity) context).findViewById(R.id.editTextIpe);
        ips.setText("");
        new Thread(new Runnable() {
            public void run() {
                myNet.udpSend(ipbrc,"ip=1");
            }
        }).start();
        if(pah==5) {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.aaa);
            mp.start();
        }
    }

    public void onButton_SendFileClick(View v){
        Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_PICK);
        String[] mimetypes = {"image/bmp", "text/plain"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
    }

    public void onButton_SelAudClick(View v){
        Intent intent = new Intent().setType("audio/mp3").setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Select a file"), 124);
    }

    Uri music;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 123 && resultCode == RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file
            File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath());
            Toast.makeText(context,  "dir "+dir, Toast.LENGTH_LONG).show();
            final File file = new File(dir + selectedfile.getPath());
            String ipe = "";
            if (mSettings.contains("ipe")) ipe = mSettings.getString("ipe", "");
            if (!ipe.isEmpty()) {
                Toast.makeText(context,  "Файл отправлен, ждите ответа", Toast.LENGTH_LONG).show();
                String[] ips = ipe.split("\n");
                if(ips.length==1){
                    sendFile(ips[0], file);
                }
                if(ips.length>1){
                    sendFile(ips[0], file);
                    sendFile(ips[1], file);
                }
            } else {
                Toast.makeText(context,  "На вкладке IP не вбиты адреса", Toast.LENGTH_LONG).show();
            }
        }
        if(requestCode == 124 && resultCode == RESULT_OK) {
            music = data.getData(); //The uri with the location of the file
            TextView an = findViewById(R.id.textViewAudName);
            List<String> ps = music.getPathSegments();
            an.setText(ps.get(ps.size() - 1));
        }
    }

    MediaPlayer mpp;
    CountDownTimer audw;

    public void onButton_RunAudClick(View v){
        try {
            final String ipbrc = myNet.brIP(getBaseContext());
            final EditText Rd = findViewById(R.id.editTextDelay);
            int Rdt = Integer.parseInt(Rd.getText().toString());
            if (Rdt > 0) {
                Toast.makeText(context, "Отсчет "+Rdt, Toast.LENGTH_SHORT).show();
                audw = new CountDownTimer(Rdt * 1000, 1000){
                    public void onTick(long millisUntilFinished) {
                        long cd = millisUntilFinished / 1000;
                        Rd.setText(String.valueOf(cd));
                    }
                    public void onFinish() {
                        try {
                            final String ipbrc1 = myNet.brIP(getBaseContext());
                            Log.d("IPBRC", ipbrc);
                            new Thread(new Runnable() {
                                public void run() {
                                    myNet.udpSend(ipbrc, "go=1");
                                }
                            }).start();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Ошибка 1 "+e.getMessage() + "\n проверьте ip", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            if (mpp != null) mpp.stop();
                            mpp = MediaPlayer.create(context, music);
                            mpp.start();
                        }  catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Ошибка "+e.getMessage() + "\n укажите файл", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.start();
            } else {
                new Thread(new Runnable() {
                    public void run() {
                        myNet.udpSend(ipbrc, "go=1");
                    }
                }).start();
                try {
                    if (mpp != null) mpp.stop();
                    mpp = MediaPlayer.create(this, music);
                    mpp.start();
                }  catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Ошибка "+e.getMessage() + "\n укажите файл", Toast.LENGTH_SHORT).show();
                }
            }
        }  catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Ошибка "+e.getMessage() + "\n проверьте ip", Toast.LENGTH_SHORT).show();
        }
    }

    public void onButton_StpAudClick(View v){
        try {
            final String ipbrc =  myNet.brIP(getBaseContext());
            new Thread(new Runnable() {
                public void run() {
                    myNet.udpSend(ipbrc,"stp=1");
                }
            }).start();
        }  catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Ошибка udp "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        try {
            if (mpp != null) mpp.stop();
        }  catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Ошибка плеера "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        try {
            if (audw != null) audw.cancel();
        }  catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Ошибка таймера "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        EditText Rd = findViewById(R.id.editTextDelay);
        Rd.setText("3");
    }

    public void onCheckBox_CashingClick(View v){
        CheckBox cch = findViewById(R.id.checkBoxCashing);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean("cch", cch.isChecked());
        editor.apply();
        WebView webView1 = ((Activity) context).findViewById(R.id.ww1);
        WebView webView2 = ((Activity) context).findViewById(R.id.ww2);
        if(cch.isChecked()){
            webView1.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
            webView2.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        } else {
            webView1.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            webView2.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
    }

    private void sendFile(final String ip, final File file){
        new Thread(new Runnable() {
            public void run() {
                try {
                    final String result = myNet.postFile("http://"+ip+"/load", file);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(context, myNet.getIpFromString(ip)+": "+result, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e){
                    e.printStackTrace();
                    final Exception ef = e;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(context,  "err "+myNet.getIpFromString(ip)+": " + ef.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    public void onButton_PicsUpdateClick(View v){
        WebView webView1 = ((Activity) context).findViewById(R.id.ww1);
        WebView webView2 = ((Activity) context).findViewById(R.id.ww2);
        webView1.reload();
        webView2.reload();
    }

    public void onButton_PicsHomeClick(View v){
        WebView webView1 = ((Activity) context).findViewById(R.id.ww1);
        WebView webView2 = ((Activity) context).findViewById(R.id.ww2);
        webView1.getSettings().setUseWideViewPort(true);
        webView1.getSettings().setLoadWithOverviewMode(true);
        webView2.getSettings().setUseWideViewPort(true);
        webView2.getSettings().setLoadWithOverviewMode(true);
        String a1 = myNet.getIpFromString(webView1.getUrl(),true);
        String a2 = myNet.getIpFromString(webView2.getUrl(), true);
        webView1.loadUrl("http://" + a1 + "/");
        webView2.loadUrl("http://" + a2 + "/");
    }

    private Integer cnfWait = 1;
    private Integer cnfBrgn = 8;
    private Integer cnfWhodr = 1;
    private Integer cnfProg = 1;
    private Integer cnfProgm = 0;
    private Integer cnfWhodr3 = 1;
    private Integer cnfWhodr3m = 0;
    private String cnfMode = "loop";
    private Integer cnfEnow = 0;

    public void onControl(View v){
        String ipe = "";
        if (mSettings.contains("ipe")) ipe = mSettings.getString("ipe", "");
        final String qw;
        EditText eBpm = findViewById(R.id.editTextBpm);
        switch(v.getId()) {
            case R.id.buttonGo:
            case R.id.imageButtonGo: qw = "go=1"; break;
            case R.id.imageButtonStop: qw = "stp=1"; break;
            case R.id.buttonBtt: qw = "vcc=1"; break;
            case R.id.buttonDelSub: if(cnfWait>1  ) cnfWait--; qw = "spd="+cnfWait; break;
            case R.id.buttonDelAdd: if(cnfWait<250) cnfWait++; qw = "spd="+cnfWait; break;
            case R.id.buttonDrawIp: qw = "drip=1";break;
            case R.id.buttonBrgSub: if(cnfBrgn>8  ) cnfBrgn-=4; qw = "brg="+cnfBrgn; break;
            case R.id.buttonBrgAdd: if(cnfBrgn<250) cnfBrgn+=4; qw = "brg="+cnfBrgn; break;
            case R.id.buttonMode1: cnfMode = "loop"; qw = "btmode=loop"; break;
            case R.id.buttonMode2: cnfMode = "one";  qw = "btmode=one"; break;
            case R.id.buttonFiles: qw = "mode=3"; cnfWhodr = 3; break;
            case R.id.buttonProg: qw = "mode=4"; cnfWhodr = 4; break;
            case R.id.buttonProgSw1:
                switch (cnfWhodr){
                    case 3:  Toast.makeText(context, "нельзя листать в режиме файлов", Toast.LENGTH_SHORT).show(); qw = ""; break;
                    case 4: if(cnfProg==1) cnfProg=cnfProgm; else cnfProg--; qw = "prg="+cnfProg; break;
                    default: qw="";
                }
                break;
            case R.id.buttonProgSw2:
                switch (cnfWhodr){
                    case 3: Toast.makeText(context, "нельзя листать в режиме файлов", Toast.LENGTH_SHORT).show(); qw = ""; break;
                    case 4: if(cnfProg==cnfProgm) cnfProg=1; else cnfProg++; qw = "prg="+cnfProg; break;
                    default: qw="";
                }
                break;
            case R.id.buttonSw1:
                if(cnfMode.equals("loop")){
                    Toast.makeText(context, "переключите в режим по 1", Toast.LENGTH_SHORT).show();
                    qw="";
                } else {
                    switch (cnfWhodr){
                        case 3: if(cnfWhodr3==1) cnfWhodr3=cnfWhodr3m; else cnfWhodr3--; qw = "modeone="+cnfWhodr3; break;
                        case 4: Toast.makeText(context, "нельзя листать в режиме программы", Toast.LENGTH_SHORT).show(); qw = ""; break;
                        default: qw="";
                    }
                }
                break;
            case R.id.buttonSw2:
                if(cnfMode.equals("loop")){
                    Toast.makeText(context, "переключите в режим по 1", Toast.LENGTH_SHORT).show();
                    qw="";
                } else {
                    switch (cnfWhodr){
                        case 3: if(cnfWhodr3==cnfWhodr3m) cnfWhodr3=1; else cnfWhodr3++; qw = "modeone="+cnfWhodr3; break;
                        case 4: Toast.makeText(context, "нельзя листать в режиме программы", Toast.LENGTH_SHORT).show(); qw = ""; break;
                        default: qw="";
                    }
                }
                break;
            case R.id.buttonVer: qw = "ver=1";break;
            case R.id.buttonCmt:
            case R.id.buttonCmt2: qw = "cmt=1";break;
            case R.id.buttonRst: qw = "rst=1";break;
            case R.id.buttonWaitWfY: qw = "skwf=1";break;
            case R.id.buttonWaitWfN: qw = "skwf=0";break;
            case R.id.buttonCont0: qw = "cont=0";break;
            case R.id.buttonCont32: qw = "cont=32";break;
            case R.id.buttonCont64: qw = "cont=64";break;
            case R.id.buttonMacapY: qw = "macun=1";break;
            case R.id.buttonMacapN: qw = "macs=1";break;
            case R.id.buttonLisY: qw = "uselis=1";break;
            case R.id.buttonLisN: qw = "uselis=0";break;
            case R.id.buttonLisHitY: qw = "hitlis=1";break;
            case R.id.buttonLisHitN: qw = "hitlis=0";break;
            case R.id.buttonLisBrgY: qw = "brglis=1";break;
            case R.id.buttonLisBrgN: qw = "brglis=0";break;
            case R.id.buttonLisSpdY: qw = "spdlis=1";break;
            case R.id.buttonLisSpdN: qw = "spdlis=0";break;
            case R.id.buttonSetBpm: qw = "bpm=" + eBpm.getText(); break;
            case R.id.buttonReboot: qw = "restart=1";break;
            default: qw="";
        }
        CheckBox p1 = findViewById(R.id.checkBoxUdp);
        CheckBox r1 = findViewById(R.id.checkBoxRpt1);

        if(qw.length() == 0) return;
        if(p1 == null || p1.isChecked()) {
            final String ipbrc = myNet.brIP(getBaseContext());
            new Thread(new Runnable() {
                public void run() {
                    myNet.udpSend(ipbrc,qw);
                }
            }).start();
            if(r1 != null && r1.isChecked()) new Thread(new Runnable() {
                public void run() {
                    myNet.udpSend(ipbrc,qw);
                }
            }).start();
        } else {
            if (!ipe.isEmpty()) {
                String[] ips = ipe.split("\n");
                for (String a : ips) {
                    if (myNet.checkIp(getBaseContext(), a, false))
                        new httpGet(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "http://" + a + "/req?" + qw);
                }
            }
        }
        if(pah==5) {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.kurl);
            mp.start();
        }
    }

    public void clearAns(View v){
        TextView t = v.findViewById(R.id.textViewAns);
        t.setText("");
    }

    public static Integer pah = 0;

    public void Pahom(View v){
        if(pah<5) {
            pah++;
            if (pah == 5) {
                //v.setBackgroundDrawable(getDrawable(R.drawable.p2));
                View vp = v.getRootView();
                ImageView im = vp.findViewById(R.id.bkHelp);
                im.setImageDrawable(getDrawable(R.drawable.p2));
                MediaPlayer mp = MediaPlayer.create(this, R.raw.zdra);
                mp.start();
            }
        } else {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.zaeb);
            mp.start();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private MyDatagramReceiver myDatagramReceiver = null;

    public boolean dbgMenu(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_dbgstate:
                if(myDatagramReceiver == null) Toast.makeText(context, "its null", Toast.LENGTH_SHORT).show();
                else if(myDatagramReceiver.isAlive()) Toast.makeText(context, "alive", Toast.LENGTH_SHORT).show();
                else Toast.makeText(context, "stopped", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_dbgrun:
                if(myDatagramReceiver.isAlive()){
                    Toast.makeText(context, "already runned", Toast.LENGTH_SHORT).show();
                } else {
                    myDatagramReceiver = new MyDatagramReceiver();
                    myDatagramReceiver.start();
                    Toast.makeText(context, "ok", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_dbgstop:
                myDatagramReceiver.kill();
                Toast.makeText(context, "stopped", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    protected void onResume() {
        super.onResume();
        //myDatagramReceiver = new MyDatagramReceiver();
        //myDatagramReceiver.start();
        Log.d("APP","Resume");
    }

    protected void onPause() {
        super.onPause();
        //myDatagramReceiver.kill();
        Log.d("APP","Pause");
    }

    private class MyDatagramReceiver extends Thread {
        private static final int MAX_UDP_DATAGRAM_LEN = 64;
        private static final int UDP_SERVER_PORT = 60201;
        private boolean bKeepRunning = true;
        private String lastMessage = "";
        List<String> listMessage = new ArrayList<String>();

        public void run() {

            String message;
            byte[] lmessage = new byte[MAX_UDP_DATAGRAM_LEN];
            DatagramPacket packet = new DatagramPacket(lmessage, lmessage.length);

            InetAddress uip;
            DatagramSocket socket = null;

            try {
                socket = new DatagramSocket(null);
                socket.setReuseAddress(true);
                socket.setBroadcast(true);
                socket.bind(new InetSocketAddress(UDP_SERVER_PORT));

                while (bKeepRunning) {
                    socket.receive(packet);
                    uip = packet.getAddress();
                    message = new String(lmessage, 0, packet.getLength());
                    lastMessage = myNet.getIpFromString(uip.toString()) + ": " + message;
                    Log.d("UDP",lastMessage);
                    listMessage.add(lastMessage);
                    runOnUiThread(updateTextMessage);
                }
            } catch (Throwable e) {
                Log.e("UDPERR", e.getMessage());
                e.printStackTrace();
            }

            if (socket != null) {
                socket.close();
            }
        }

        public void kill() {
            bKeepRunning = false;
        }

        public String getLastMessage() {
            if(listMessage.isEmpty()==false) {
                String m = listMessage.get(0);
                listMessage.remove(0);
                return m;
            }
            return lastMessage;
        }
    }

    private Runnable updateTextMessage = new Runnable() {
        public void run() {
            if (myDatagramReceiver == null) return;
            String msg = myDatagramReceiver.getLastMessage();
            TextView ans = ((Activity) context).findViewById(R.id.textViewAns);
            EditText ips = ((Activity) context).findViewById(R.id.editTextIpe);
            if(ans instanceof TextView) {
                ans.append("\n" + msg);
            } else if(ips instanceof  EditText) {
                msg = myNet.getIpFromString(msg,true);
                String chip = ips.getText().toString();
                if(chip.contains(msg)) return;
                if(ips.getText().length()==0) ips.append(msg); else ips.append("\n" + msg);
            } else {
                Toast.makeText(context, myDatagramReceiver.getLastMessage(), Toast.LENGTH_SHORT).show();
            }
            parseConf(msg);
        }
    };

    public void parseConf(String txt){
        if(txt.contains("runned")){
            Toast.makeText(context, "есть настройки", Toast.LENGTH_SHORT).show();
            try {
                String[] c = txt.split("\\s+");
                cnfWait = Integer.parseInt(c[2]);
                cnfBrgn = Integer.parseInt(c[3]);
                cnfWhodr = Integer.parseInt(c[4]);
                cnfProg = Integer.parseInt(c[5]);
                cnfProgm = Integer.parseInt(c[6]);
                cnfWhodr3 = 0; //Integer.parseInt(c[7]);
                cnfWhodr3m = Integer.parseInt(c[8]);
                cnfEnow = Integer.parseInt(c[9]);
            }  catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Ошибка "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        if(txt.contains("myip")){
            try {
                String[] c = txt.split("\\s+");
                String newIp  = c[2];
                if (myNet.checkIp(getBaseContext(), newIp, false)) {
                    String ipe = "";
                    if (mSettings.contains("ipe")) ipe = mSettings.getString("ipe", "");
                    String[] ips = ipe.split("\n");
                    Boolean alr = false;
                    for (String a : ips) {
                        if(a.equals(newIp)) alr = true;
                    }
                    if(alr){
                        Toast.makeText(context, "Уже есть " + newIp, Toast.LENGTH_SHORT).show();
                    } else {
                        SharedPreferences.Editor editor = mSettings.edit();
                        if(firstAddIp){
                            editor.putString("ipe", newIp);
                            firstAddIp = false;
                        } else {
                            editor.putString("ipe", ipe.length() > 0 ? (ipe + "\n" + newIp) : newIp);
                        }
                        editor.apply();
                        Toast.makeText(context, "Добавлен " + newIp, Toast.LENGTH_SHORT).show();
                    }
                }
            }  catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Ошибка "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
