package io.complicated.stereostream.api.room;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Locale;

import static io.complicated.stereostream.utils.GsonSingleton.getGson;

public final class RoomWithLog /*extends Room*/ implements Cloneable {
    @SerializedName("log")
    private LogEntry[] mLogEntry;

    @SerializedName("owner")
    private String mOwner;

    @SerializedName("name")
    private String mName;

    public RoomWithLog(final String name, final String owner, final String[] log) {
        mName = name;
        mOwner = owner;
        // super(name, owner);
        if (log == null || log.length < 1) return;

        final LogEntry[] logEntries = new LogEntry[log.length];
        for (int i = 0; i < log.length; i++)
            logEntries[i] = LogEntry.fromString(log[i]);
        mLogEntry = logEntries;
    }

    public RoomWithLog(final String name) {
        this(name, null, null);
    }

    public static RoomWithLog fromString(final String s) {
        return s == null ? null : getGson().fromJson(s.startsWith("{") ? s :
                s.substring(s.indexOf("{")), RoomWithLog.class);
    }

    public final String getLogStr() {
        if(mLogEntry == null || mLogEntry.length < 1) return "";
        String s = "";
        for (final LogEntry logEntry : mLogEntry)
            s = String.format(Locale.getDefault(),
                    "%s\n%s",
                    logEntry.getContent(), s
            );
        return s;
    }

    public final LogEntry[] getLogEntries() {
        return mLogEntry;
    }

    @Override
    @NonNull
    public final String toString() {
        return String.format(Locale.getDefault(),
                "RoomWithLog{\"name\": \"%s\", \"owner\": \"%s\", \"log\": \"%s\"}",
                mName, mOwner,
                mLogEntry != null && mLogEntry.length > 0 ? Arrays.toString(mLogEntry) : "[]"
        );
    }
}
