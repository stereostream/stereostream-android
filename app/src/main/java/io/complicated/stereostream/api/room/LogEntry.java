package io.complicated.stereostream.api.room;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Locale;

import static io.complicated.stereostream.utils.GsonSingleton.getGson;

public final class LogEntry implements Cloneable {
    @SerializedName("date")
    private Date mDate;

    @SerializedName("user")
    private String mUser;

    @SerializedName("content")
    private String mContent;


    public LogEntry(final String date_s, final String user, final String content) {
        mDate = new Date(date_s);
        mUser = user;
        mContent = content;
    }

    public static LogEntry fromString(final String s) {
        return s == null ? null : getGson().fromJson(s.startsWith("{") ? s :
                s.substring(s.indexOf("{")), LogEntry.class);
    }

    final public String getContent() {
        return mContent;
    }

    public final Date getCreatedAt() {
        return mDate;
    }

    public final String getUserStr() {
        return mUser;
    }

    @Override
    @NonNull
    public final String toString() {
        return String.format(Locale.getDefault(),
                "LogEntry{\"date\": \"%s\", \"user\": \"%s\", \"content\": \"%s\"}",
                mDate, mUser, mContent
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
