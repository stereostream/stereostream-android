package io.complicated.stereostream.chat;

import com.stfalcon.chatkit.messages.MessageInput;

final class MessageListAdapter implements MessageInput.InputListener {
    @Override
    public boolean onSubmit(CharSequence input) {
        /*messagesAdapter.addToStart(
                MessagesFixtures.getTextMessage(input.toString()), true);*/
        return true;
    }
}
