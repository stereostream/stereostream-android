package io.complicated.stereostream.utils;

import java.util.Locale;

/**
 * Created by samuel on 4/12/17.
 */

public final class HttpCodeException extends Exception {
    public HttpCodeException() {
    }

    // Constructor that accepts a message
    public HttpCodeException(int http_code) {
        super(String.format(Locale.getDefault(), "%d", http_code));
    }
}
