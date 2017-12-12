package io.complicated.stereostream.utils;

import java.io.IOException;

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
}
