package io.complicated.stereostream.api;

import android.content.Context;

import io.complicated.stereostream.utils.BaseApiClient;
import io.complicated.stereostream.utils.CachedReq;
import okhttp3.Request;

public final class ServerClient extends BaseApiClient {
    private ServerClient(final Context context, final String hostname, final CachedReq cache) {
        super(context, hostname, cache);
    }

    public ServerClient(final Context context) {
        this(context, null, null);
    }

    public final Request get_version() {
        return new Request.Builder()
                .url(getBaseUri())
                .get()
                .build();
    }
}
