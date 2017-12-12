package io.complicated.stereostream.api.room;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.net.ConnectException;
import java.util.Locale;

import io.complicated.stereostream.utils.ApiClient;
import io.complicated.stereostream.utils.BaseApiClient;
import io.complicated.stereostream.utils.ErrorOrEntity;
import io.complicated.stereostream.utils.GsonSingleton;
import io.complicated.stereostream.utils.IMimeTypes;
import okhttp3.Request;
import okhttp3.RequestBody;

public final class RoomsClient extends BaseApiClient {
    private final String mApiPrefix = "/room";
    private final Gson mGson = GsonSingleton.getGson();

    public RoomsClient(final Context context, final String accessToken) throws ConnectException {
        super(context, null, null, null, null, true, accessToken);
    }

    public final Request get() {
        return new Request.Builder()
                .url(getBaseUri() + mApiPrefix)
                .get()
                .build();
    }

    public final Request post(final Room room) {
        return new Request.Builder()
                .url(String.format(Locale.getDefault(),
                        "%s%s/%s", getBaseUri(), mApiPrefix, room.getName()))
                .post(RequestBody.create(null, new byte[]{}))
                .build();
    }

    public final ErrorOrEntity<Room> postSync(final Room room) {
        return ApiClient.sync(getClient(), post(room), Room.class);
    }

    public final ErrorOrEntity<ListRooms> getSync() {
        Log.d("RoomsClient", "getSync");
        return ApiClient.sync(getClient(), get(), ListRooms.class);
    }
}
