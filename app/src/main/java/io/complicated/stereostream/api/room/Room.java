package io.complicated.stereostream.api.room;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

import io.complicated.stereostream.utils.Slugify;

import static io.complicated.stereostream.utils.GsonSingleton.getGson;

public class Room implements Cloneable {
    @SerializedName("name")
    protected String mName;
    @SerializedName("owner")
    protected String mOwner;

    public Room(final String name, final String owner) {
        final Slugify slg = new Slugify();
        mName = slg.slugify(name.replace("_", "-"));
        mOwner = owner;
    }

    public Room(final String name) {
        this(name, null);
    }

    public static Room fromString(final String s) {
        return s == null ? null : getGson().fromJson(s.startsWith("{") ? s :
                s.substring(s.indexOf("{")), Room.class);
    }

    public static Room[] fromStringArray(final String s) {
        if (s == null) return null;
        return getGson().fromJson(s, Room[].class);
    }

    @NonNull
    public final String getName() {
        return mName == null ? "" : mName;
    }

    public final void setName(final String name) {
        mName = name;
    }

    @NonNull
    public final String getOwner() {
        return mOwner != null ? mOwner : "";
    }

    public final void setOwner(final String owner) {
        mOwner = owner;
    }

    @Override
    @NonNull
    public String toString() {
        return String.format(Locale.getDefault(),
                "Room{\"name\": \"%s\", \"owner\": \"%s\"}",
                mName, mOwner
        );
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            e.printStackTrace(System.err);
            return this;
        }
    }
}
