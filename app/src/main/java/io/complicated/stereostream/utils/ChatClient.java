package io.complicated.stereostream.utils;

import android.util.Log;

import java.net.URISyntaxException;
import java.util.Locale;
import java.util.logging.Logger;

import io.socket.client.IO;
import io.socket.emitter.Emitter;

import static io.socket.client.Socket.EVENT_CONNECT;
import static io.socket.client.Socket.EVENT_DISCONNECT;

public class ChatClient {
    private final io.socket.client.Socket mSocket;

    public ChatClient(final String baseUri) throws URISyntaxException {
        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnection = true;
        mSocket = IO.socket(baseUri, opts);
        mSocket
                .on(EVENT_CONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        mSocket.emit("chat message", "fdfdsf User connected");
                        // mSocket.disconnect();
                    }
                })
                .on("event", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                    }

                })
                .on(EVENT_DISCONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                    }

                });
        mSocket.connect();
    }

    public void connect() {
        mSocket.connect();
    }

    public void disconnect() {
        mSocket.disconnect();
    }

    public void send(final String msg) {
        mSocket.connect();
        Log.d("ChatClient::send",
                String.format(Locale.getDefault(),
                        "mSocket.connected() = %s",
                        mSocket.connected()
                ));
        mSocket.emit("chat message", msg);
        mSocket.disconnect();
    }
}
