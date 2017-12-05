package io.complicated.stereostream.api.room;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

import io.complicated.stereostream.utils.Slugify;

import static android.text.TextUtils.join;
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

    @NonNull
    public final String getName() {
        return mName == null ? "" : mName;
    }

    public final void setName(final String name) {
        mName = name;
    }

    public final void setOwner(final String owner) {
        mOwner = owner;
    }

    public final String getOwner() {
        return mOwner != null ? mOwner : "";
    }

    @Override
    @NonNull
    public String toString() {
        return String.format(Locale.getDefault(),
                "Room{\"name\": \"%s\", \"owner\": \"%s\"}",
                mName, mOwner
        );
    }

    public static Room fromString(final String s) {
        return s == null ? null : getGson().fromJson(s.startsWith("{") ? s :
                s.substring(s.indexOf("{")), Room.class);
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
