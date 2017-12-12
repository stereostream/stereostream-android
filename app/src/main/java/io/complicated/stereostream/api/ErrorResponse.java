package io.complicated.stereostream.api;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;


public class ErrorResponse extends Exception {
    @SerializedName(value="error", alternate={"code"})
    private String mError;

    @SerializedName(value="error_message", alternate={"message", "description"})
    private String mErrorMessage;

    private ErrorResponse() {
    }

    public ErrorResponse(final String error, final String errorMessage) {
        mError = error;
        mErrorMessage = errorMessage;
    }

    @Override
    public final String toString() {
        return String.format(Locale.getDefault(), "%s: %s", mError, mErrorMessage);
    }
}
