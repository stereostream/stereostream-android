package io.complicated.stereostream.chat;


import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.ArrayList;
import java.util.Date;

public final class DefaultDialog implements IDialog<MessageLogEntry> {
    final String mId;
    final String mDialogName;
    int mUnreadCount;

    MessageLogEntry mLastMessage;
    ArrayList<IUser> mUsers;

    public DefaultDialog(final String id,
                         final String dialogName,
                         final int unreadCount) {
        mId = id;
        mDialogName = dialogName;
        mUnreadCount = unreadCount;
    }

    public DefaultDialog(final Date date,
                         final String user,
                         final String message) {
        mId = mDialogName = user;
        mUsers.add(new User(user));
        mLastMessage = new MessageLogEntry(date, user, message);
        mUnreadCount++;
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getDialogPhoto() {
        return null;
    }

    @Override
    public String getDialogName() {
        return mDialogName;
    }

    @Override
    public ArrayList<IUser> getUsers() {
        return mUsers;
    }

    public void setUsers(final ArrayList<IUser> users) {
        mUsers = users;
    }

    @Override
    public MessageLogEntry getLastMessage() {
        return mLastMessage;
    }

    @Override
    public void setLastMessage(final MessageLogEntry lastMessage) {
        mLastMessage = lastMessage;
    }

    @Override
    public int getUnreadCount() {
        return mUnreadCount;
    }
}
