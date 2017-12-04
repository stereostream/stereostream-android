package io.complicated.stereostream.utils;

import java.io.IOException;

import okhttp3.Response;

public final class StringUtils {
    public static String tryGetResponseStr(final Response response) {
        try {
            final String str = response.body().string();
            response.body().close();
            return str;
        } catch (IOException | NullPointerException _) {
            return "";
        }
    }
}
