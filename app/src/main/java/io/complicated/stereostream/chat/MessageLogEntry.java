package io.complicated.stereostream.chat;

import android.util.Log;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.Date;
import java.util.Locale;

import io.complicated.stereostream.api.room.LogEntry;


public final class MessageLogEntry implements IMessage {
    private final Date mDate;
    private final User mUser;
    private final String mText;

    public MessageLogEntry(final Date date, final String user, final String content) {
        assert date != null;
        Log.d("MessageLogEntry", date.toString());
        mDate = date;
        mUser = new User(user);
        mText = content;
    }

    public MessageLogEntry(final LogEntry logEntry) {
        mDate = logEntry.getCreatedAt();
        mUser = new User(logEntry.getUserStr());
        mText = logEntry.getContent();
    }

    @Override
    public final String getId() {
        return mUser.getId();
    }

    @Override
    public final User getUser() {
        return mUser;
    }

    @Override
    public final String getText() {
        return mText;
    }

    @Override
    public final Date getCreatedAt() {
        return mDate;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "MessageLogEntry{date: %s, user: %s, content: %s}",
                getCreatedAt(), getUser().toString(), getText());
    }
}
