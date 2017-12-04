package io.complicated.stereostream.api;

import android.content.Context;

import io.complicated.stereostream.utils.BaseApiClient;
import io.complicated.stereostream.utils.CachedReq;
import io.complicated.stereostream.utils.IMimeTypes;
import okhttp3.Request;
import okhttp3.RequestBody;

public final class MessageClient extends BaseApiClient {
    private final String mApiPrefix = "/message/";

    private MessageClient(final Context context, final String hostname, final CachedReq cache) {
        super(context, hostname, cache);
    }

    public MessageClient(final Context context) {
        this(context, null, null);
    }

    public final Request get(final String to) {
        return new Request.Builder()
                .url(getBaseUri() + mApiPrefix + to)
                .get()
                .build();
    }

    public final Request post(final String to, final String message) {
        return new Request.Builder()
                .url(getBaseUri() + mApiPrefix + to)
                .post(RequestBody.create(IMimeTypes.MEDIA_TYPE_JSON,
                        String.format("{\"to\": \"%s\", \"message\": \"%s\"}", to, message)))
                .build();
    }
}
