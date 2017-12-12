package io.complicated.stereostream.api.room;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Locale;

import static io.complicated.stereostream.utils.GsonSingleton.getGson;

public final class ListRooms {
    @SerializedName("rooms")
    private Room[] mRooms;
    @SerializedName("owner")
    private String mOwner;

    private ListRooms() {
    }

    public ListRooms(final String owner, final Room[] rooms) {
        mOwner = owner;
        mRooms = rooms;
    }

    public final Room[] getRooms() {
        return mRooms;
    }

    public final String getOwner() {
        return mOwner;
    }

    @Override
    public final String toString() {
        return String.format(Locale.getDefault(), "ListRooms{owner %s, rooms: %s}",
                mOwner, Arrays.toString(mRooms));
    }

    public static ListRooms fromString(final String s) {
        return s == null ? null :
                getGson().fromJson(s.startsWith("{") ? s : s.substring(s.indexOf("{")), ListRooms.class);
    }

    public static Room[] roomArrayfromString(final String s) {
        return s == null ? null : getGson().fromJson(s, Room[].class);
    }
}
