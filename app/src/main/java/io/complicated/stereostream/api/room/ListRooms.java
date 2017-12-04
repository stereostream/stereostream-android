package io.complicated.stereostream.api.room;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Locale;

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
}
