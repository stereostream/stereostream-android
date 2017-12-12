package io.complicated.stereostream.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Finds the network of the IP address
 * - obvious security implications
 */

final public class NetworkFinder {
    private final WeakReference<Activity> mWeakActivity;


    NetworkFinder(final Activity activity) {
        mWeakActivity = new WeakReference<>(activity);
    }

    /*final ArrayList<String> getIps() {
        final String[] ips = {};

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wm.getConnectionInfo();
        int ipAddress = connectionInfo.getIpAddress();
        String ipString = Formatter.formatIpAddress(ipAddress);
        Log.e(TAG, "activeNetwork: " + String.valueOf(activeNetwork));
        Log.e(TAG, "ipString: " + String.valueOf(ipString));
        String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
        Log.e(TAG, "prefix: " + prefix);
        for (int i = 0; i < 255; i++) {
            String testIp = prefix + String.valueOf(i);
            boolean failed = true;
            try {
                InetAddress name = InetAddress.getByName(testIp);
                String hostName = name.getCanonicalHostName();
                if (name.isReachable(1000))
                    failed = false;
                Log.e(TAG, "Host:" + hostName);
            } catch (IOException e) {
                // pass
            } finally {
                if (!failed) ips hostName;
            }
        }
    }

    ublic

    static ArrayList<String> serviceScanner() {
        ArrayList<String> servers = new ArrayList<String>();

        // Get the IP of the local machine
        String iIPv4 = "";
        String test = "";


        //getLocalIpAddress();
        //System.out.println(test);

        try {
            // Get localhost
            InetAddress addr = InetAddress.getLocalHost();

            // Get IP Address
            byte[] ipAddr = addr.getAddress();

            iIPv4 = addr.toString();
            iIPv4 = iIPv4.substring(iIPv4.indexOf("/") + 1);
            iIPv4 = "10.0.2.1";
        } catch (UnknownHostException e) {
            // Exception output
        }
        // IP stuff.
        String IPv4Start = "", IPv4End = "";
        iIPv4 = iIPv4.substring(0, iIPv4.lastIndexOf("."));
        iIPv4 += ".";
        IPv4Start = iIPv4 + "1";
        IPv4End = iIPv4 + "254";


        PrintWriter out = null;
        BufferedReader in = null;

        // Loop to scan each address on the local subnet
        for (int i = 1; i < 255; i++) {

            try {
                System.out.println(iIPv4 + i);
                SocketWrapper mySocket = new SocketWrapper();
                SocketAddress address = new InetSocketAddress(iIPv4 + i, port);

                mySocket.connect(address, 5);

                out = new PrintWriter(mySocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(
                        mySocket.getInputStream()));
                out.println("Scanning!");
                String fromServer;
                while ((fromServer = in.readLine()) != null) {
                    System.out.println("Server: " + fromServer);
                    if (fromServer.equals("Server here!")) {
                        servers.add(iIPv4 + i);
                        mySocket.close();
                        break;
                    }
                }
                mySocket.close();
                out.close();
                in.close();

            } catch (UnknownHostException e) {
            } catch (IOException e) {
            }
        }
        return servers;
    }*/
}
