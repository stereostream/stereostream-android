package io.complicated.stereostream.api.room;

import android.content.Context;

import com.google.gson.Gson;

import java.net.ConnectException;
import java.util.Locale;

import io.complicated.stereostream.utils.ApiClient;
import io.complicated.stereostream.utils.BaseApiClient;
import io.complicated.stereostream.utils.CachedReq;
import io.complicated.stereostream.utils.ErrorOrEntity;
import io.complicated.stereostream.utils.IMimeTypes;
import okhttp3.Request;
import okhttp3.RequestBody;

import static io.complicated.stereostream.utils.GsonSingleton.getGson;

public final class RoomClient extends BaseApiClient {
    private final String mApiPrefix = "/room";
    private final Gson mGson = getGson();

    private RoomClient(final Context context, final String hostname,
                       final CachedReq cache) throws ConnectException {
        super(context, hostname, cache);
    }

    public RoomClient(final Context context) throws ConnectException {
        this(context, null, null);
    }

    public RoomClient(final Context context, final String accessToken) throws ConnectException {
        super(context, null, null, null, null, true, accessToken);
    }

    public final Request get(final String nameOrEmail) {
        return new Request.Builder()
                .url(getBaseUri() + mApiPrefix + "/" + nameOrEmail)
                .get()
                .build();
    }

    public final ErrorOrEntity<Room> getSync(final String nameOrEmail) {
        return ApiClient.sync(getClient(), get(nameOrEmail), Room.class);
    }

    public final Request get(final Room room) {
        return new Request.Builder()
                .url(getBaseUri() + mApiPrefix + "/" + room.getName())
                .get()
                .build();
    }

    public final ErrorOrEntity<RoomWithLog> getSync(final Room room) {
        return ApiClient.sync(getClient(), get(room), RoomWithLog.class);
        /*
        try {
            Log.d("ApiClient", ApiClient.sync(getClient(), get(room)).getResponse().toString());
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        */
    }

    public final Request put(final Room prevRoom, final Room newRoom) {
        return new Request.Builder()
                .url(String.format(Locale.getDefault(), "%s%s/%s_%s",
                        getBaseUri(), mApiPrefix, prevRoom.getName(), prevRoom.getOwner()
                ))
                .put(RequestBody.create(IMimeTypes.MEDIA_TYPE_JSON, mGson.toJson(newRoom)))
                .build();
    }

    public final ErrorOrEntity<Room> putSync(final Room prevRoom,
                                             final Room newRoom) {
        return ApiClient.sync(getClient(), put(prevRoom, newRoom), Room.class);
    }

    public final Request del(final Room room) {
        return new Request.Builder()
                .url(getBaseUri() + mApiPrefix + "/" + room.getName())
                .delete()
                .build();
    }

    public final ErrorOrEntity<Room> delSync(final Room room) {
        return ApiClient.sync(getClient(), del(room), Room.class);
    }
}
