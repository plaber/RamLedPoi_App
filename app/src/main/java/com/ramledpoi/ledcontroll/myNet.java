package com.ramledpoi.ledcontroll;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class myNet {
    public static String get(String url) throws Exception {
        Log.d("URL", url);
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0" );
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        connection.setRequestProperty("Content-Type", "application/json");

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = bufferedReader.readLine()) != null) {
            response.append(inputLine);
        }
        bufferedReader.close();

        return response.toString();
    }

    public static String postFile(String addr, File f) throws Exception {
        URL url = new URL(addr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String boundary = UUID.randomUUID().toString();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        int respCode = 0;

        DataOutputStream request = new DataOutputStream(connection.getOutputStream());
        request.writeBytes("--" + boundary + "\r\n");
        request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + f.getName() + "\"\r\n\r\n");
        RandomAccessFile file = new RandomAccessFile(f, "r");
        try {
            long longlength = file.length();
            int length = (int) longlength;
            byte[] data = new byte[length];
            file.readFully(data);
            request.write(data);
            file.close();//**
            request.writeBytes("\r\n");
        } finally {
            file.close();
        }

        request.writeBytes("--" + boundary + "--\r\n");
        request.flush();
        respCode = connection.getResponseCode();

        switch(respCode) {
            case 200:
                return "ok";
            case 301:
            case 302:
            case 307:
                return "redirected";
            default:
                return "httpcode: " + respCode;
        }
    }

    public static String myIP(Context context){
        //ConnectivityManager cm = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        //NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        WifiManager wm = (WifiManager)context.getSystemService(context.WIFI_SERVICE);
        WifiInfo connectionInfo = wm.getConnectionInfo();
        int ipAddress = connectionInfo.getIpAddress();
        String ipString = Formatter.formatIpAddress(ipAddress);
        return ipString;
    }

    public static String brIP(Context context){
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int apState = 0;
        try {
            apState = (Integer) wifi.getClass().getMethod("getWifiApState").invoke(wifi);
        } catch (Exception e) {

        }
        if (apState == 13) return "192.168.43.255";
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return (quads[0] & 0xFF) + "." + (quads[1] & 0xFF) + "." + (quads[2] & 0xFF) + "." + (quads[3] & 0xFF);
    }

    public static void getClientList(Context context) {
        int macCount = 0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            EditText ips = ((Activity) context).findViewById(R.id.editTextIpe);
            ips.setText("");
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null ) {
                    // Basic sanity check
                    String mac = splitted[3];
                    System.out.println("Mac : Outside If "+ mac );
                    if (mac.matches("..:..:..:..:..:..")) {
                        macCount++;
                        System.out.println("Mac : "+ mac + " IP Address : "+splitted[0] );
                        System.out.println("Mac_Count  " + macCount + " MAC_ADDRESS  "+ mac);
                        //Toast.makeText(context,"Device " + macCount + " IP " + splitted[0], Toast.LENGTH_SHORT).show();
                        if(ips.getText().length()==0) ips.append(splitted[0]); else ips.append("\n" + splitted[0]);
                    }
                }
            }
        } catch(Exception e) {

        }
        if(macCount==0) Toast.makeText(context,"Устройства не найдены", Toast.LENGTH_SHORT).show();
    }

    public static boolean checkIp(Context context, String ip, boolean notice) {
        if(Patterns.IP_ADDRESS.matcher(ip).matches()){
            return true;
        } else {
            if (notice) Toast.makeText(context, "wrong ip format", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean checkIp(Context context, String ip) {
        return checkIp(context, ip, true);
    }

    public static String getIpFromString(String t, boolean full){
        String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(t);
        if (matcher.find()) {
            String ip = matcher.group();
            if(full) return ip;
            String[] ips = ip.split("\\.");
            return ips[3];
        } else{
            if(full) return "noip";
            return "0";
        }
    }

    public static String getIpFromString(String t){
        return getIpFromString(t, false);
    }

    public static void udpSend(String ip, String cmd) {
        try {
            DatagramSocket udpSocket = new DatagramSocket(8888 /*null*/);
            //udpSocket.setReuseAddress(true);
            //udpSocket.setBroadcast(true);
            //udpSocket.bind(new InetSocketAddress(8888));
            InetAddress serverAddr = InetAddress.getByName(ip);
            byte[] buf = (cmd).getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, 8888);
            udpSocket.send(packet);
            udpSocket.close();
        } catch (SocketException e) {
            Log.e("Udp:", "Socket Error:", e);
        } catch (IOException e) {
            Log.e("Udp Send:", "IO Error:", e);
        }
    }

    public static String executeCmd(String cmd, boolean sudo){
        try {

            Process p;
            if(!sudo)
                p= Runtime.getRuntime().exec(cmd);
            else{
                p= Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            }
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String s;
            String res = "";
            while ((s = stdInput.readLine()) != null) {
                res += s + "\n";
            }
            p.destroy();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean ping(String ip){
        String rp = executeCmd("ping -c 1 -w 1 "+ip, false);
        //Log.d("PING", rp);
        return rp.contains("0% packet loss");
    }

}
