package io.complicated.stereostream.chat;


import java.util.Date;
import java.util.Locale;

import static io.complicated.stereostream.utils.StringUtils.getCalendarFromISO;

public final class ChatMessage {
    private String mDateAsStr;
    private Date mDate;
    private String mAuthor;
    private String mMessage;

    public ChatMessage(final String date, final String author, final String message) {
        setDate(date);
        setAuthor(author);
        setMessage(message);
    }

    public String getDateAsStr() {
        System.out.println("");
        return mDateAsStr;
    }

    public Date getDate() {
        return mDate;
    }

    private void setDate(final String date) {
        Date parsedDate;
        parsedDate = getCalendarFromISO(date).getTime();
        /*TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.getDefault());
        df.setTimeZone(tz);


        try {
            parsedDate = df.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            parsedDate = null;
        }


        final DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                Locale.getDefault());
        */

        /*
        try {
            parsedDate = df1.parse(date);
        } catch (ParseException e) {
            parsedDate = null;
        }
        */
        mDate = parsedDate;
        this.mDateAsStr = parsedDate == null ? date : parsedDate.toString();
    }

    public String getAuthor() {
        return mAuthor;
    }

    private void setAuthor(final String author) {
        this.mAuthor = author;
    }

    public String getMessage() {
        return mMessage;
    }

    private void setMessage(final String message) {
        this.mMessage = message;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "ChatMessage{date: %s, author: %s, message: %s}",
                getDateAsStr(), getAuthor(), getMessage());
    }
}
