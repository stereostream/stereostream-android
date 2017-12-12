package io.complicated.stereostream.api;

import android.content.Context;

import java.net.ConnectException;

import io.complicated.stereostream.utils.BaseApiClient;
import io.complicated.stereostream.utils.CachedReq;
import okhttp3.Request;

public final class ServerClient extends BaseApiClient {
    private ServerClient(final Context context, final String hostname,
                         final CachedReq cache) throws ConnectException {
        super(context, hostname, cache);
    }

    public ServerClient(final Context context) throws ConnectException {
        this(context, null, null);
    }

    public final Request get_version() {
        return new Request.Builder()
                .url(getBaseUri())
                .get()
                .build();
    }
}
