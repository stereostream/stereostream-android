package io.complicated.stereostream.utils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class NetworkUtils {
    public static boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean head(final String urlS) {
        HttpURLConnection.setFollowRedirects(false);
        try {
            final HttpURLConnection con = (HttpURLConnection) new URL(urlS).openConnection();
            try {
                con.setRequestMethod("HEAD");

                con.setConnectTimeout(5000); //set timeout to 5 seconds

                return con.getResponseCode() == HttpURLConnection.HTTP_OK;
            } catch (RuntimeException | IOException e) {
                e.printStackTrace(System.err);
            } finally {
                // Nested try/catch only way I can think of getting this method run:
                if (con != null) con.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        return false;
    }

    public static boolean get(final String urlS) {
        HttpURLConnection.setFollowRedirects(false);
        try {
            final HttpURLConnection con = (HttpURLConnection) new URL(urlS).openConnection();
            try {
                con.setRequestMethod("GET");

                con.setConnectTimeout(5000); //set timeout to 5 seconds

                return con.getResponseCode() == HttpURLConnection.HTTP_OK;
            } catch (RuntimeException | IOException e) {
                e.printStackTrace(System.err);
            } finally {
                // Nested try/catch only way I can think of getting this method run:
                if (con != null) con.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        return false;
    }
}
