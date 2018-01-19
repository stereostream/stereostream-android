package io.complicated.stereostream.chat;

import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Locale;

public class User implements IUser {
    private String mEmail;

    public User(final String email) {
        mEmail = email;
    }

    @Override
    public String getId() {
        return mEmail;
    }

    @Override
    public String getName() {
        return mEmail;
    }

    @Override
    public String getAvatar() {
        return null;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "User{id: %s, name: %s, avatar: %s}",
                getId(), getName(), getAvatar());
    }
}
