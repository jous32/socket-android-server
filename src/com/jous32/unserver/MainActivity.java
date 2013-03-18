package com.jous32.unserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
 
public class MainActivity extends Activity {
   ServerSocket ss = null;
   String mClientMsg = "";
   Thread myCommsThread = null;
   protected static final int MSG_ID = 0x1337;
   public static final int SERVERPORT = 1111;
   // default ip
   public static String SERVERIP = "10.0.2.15";
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    SERVERIP = getLocalIpAddress();
    TextView tv = (TextView) findViewById(R.id.TextView01);
    
    tv.setText("Nothing from client yet");
    this.myCommsThread = new Thread(new CommsThread());
    this.myCommsThread.start();
   }
   
   // gets the ip address of your phone's network
   private String getLocalIpAddress() {
       try {
           for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
               NetworkInterface intf = en.nextElement();
               for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                   InetAddress inetAddress = enumIpAddr.nextElement();
                   if (!inetAddress.isLoopbackAddress()) { 
                	   String sAddr = inetAddress.getHostAddress().toUpperCase();
                	   boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                	   if (isIPv4){
                		   return inetAddress.getHostAddress().toString(); 
                	   }
                	 
                	   }
               }
           }
       } catch (SocketException ex) {
           Log.e("ServerActivity", ex.toString());
       }
       return null;
   }
   @Override
   protected void onStop() {
    super.onStop();
    try {
        // make sure you close the socket upon exiting
        ss.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
   }
 
   Handler myUpdateHandler = new Handler() {
    public void handleMessage(Message msg) {
        switch (msg.what) {
        case MSG_ID:
            TextView tv = (TextView) findViewById(R.id.TextView01);
            tv.setText(mClientMsg);

            break;
        default:
            break;
        }
        super.handleMessage(msg);
    }
   };
   class CommsThread implements Runnable {
    public void run() {
        Socket s = null;
        try {
            TextView ip_tv = (TextView) findViewById(R.id.ip_textView);
            ip_tv.setText("IP: "+SERVERIP);
            ss = new ServerSocket(SERVERPORT );
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!Thread.currentThread().isInterrupted()) {
            Message m = new Message();
            m.what = MSG_ID;

            try {
                if (s == null)
                    s = ss.accept();
                BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String st = null;
                st = input.readLine();
                mClientMsg = st;
                myUpdateHandler.sendMessage(m);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    }
}
