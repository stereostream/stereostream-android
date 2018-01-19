package io.complicated.stereostream.utils;

import android.util.Log;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Response;
import okhttp3.ResponseBody;

public final class StringUtils {
    public static String tryGetResponseStr(final Response response) {
        final ResponseBody body = response.body();
        if (body != null)
            try {
                return body.string();
            } catch (IOException e) {
                return "";
            } finally {
                body.close();
            }
        return "";
    }

    public static Calendar getCalendarFromISO(String datestring) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        try {
            Date date = dateformat.parse(datestring);
            date.setHours(date.getHours() - 1);
            calendar.setTime(date);

            String test = dateformat.format(calendar.getTime());
            Log.e("TEST_TIME", test);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return calendar;
    }
}
