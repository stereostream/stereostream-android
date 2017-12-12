package io.complicated.stereostream.utils;

import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;

/**
 * SocketWrapper.io wrapper
 */

public class SocketWrapper {
    private static final String TAG = SocketWrapper.class.getSimpleName();
    private static final String SOCKET_PATH = "/socket.io";
    private static final String[] TRANSPORTS = {
            "websocket"
    };
    private static io.socket.client.Socket instance;

    /**
     * @return socket instance
     */
    public static io.socket.client.Socket getInstance(final String SOCKET_URI) {
        if (instance == null) {
            try {
                final IO.Options options = new IO.Options();
                options.path = SOCKET_PATH;
                options.transports = TRANSPORTS;
                instance = IO.socket(SOCKET_URI, options);
            } catch (final URISyntaxException e) {
                Log.e(TAG, e.toString());
            }
        }
        return instance;
    }
}
