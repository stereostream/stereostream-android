package io.complicated.stereostream.utils;

import java.util.Locale;

import io.complicated.stereostream.api.ErrorResponse;
import okhttp3.Response;

import static io.complicated.stereostream.utils.GsonSingleton.getGson;
import static io.complicated.stereostream.utils.StringUtils.tryGetResponseStr;

/**
 * ErrRes class
 */
public class ErrResResponse<F, S> extends ErrRes<F, S> {
    private final Response mResponse;
    private final ErrorResponse mErrorResponse;

    public ErrResResponse(final F error, final S result, final Response response) {
        super(error, result);

        mResponse = response;
        if (error == null && response.code() / 100 > 3)
            mErrorResponse = getGson()
                    .fromJson(tryGetResponseStr(response), ErrorResponse.class);
        else if (error != null && !(error instanceof ErrorResponse))
            mErrorResponse = getGson()
                    .fromJson(
                            String.format(Locale.getDefault(), "%s", error),
                            ErrorResponse.class
                    );
        else mErrorResponse = null;
    }

    public final Response getResponse() {
        return mResponse;
    }

    public final ErrorResponse getErrorResponse() {
        return mErrorResponse;
    }

    @Override
    public String toString() {
        final String r = String.format(Locale.getDefault(),
                "{mError: %s, mResult: %s, mResponse: {code: %d",
                getError(), getResult(), mResponse.code());

        return String.format(Locale.getDefault(), "%s, body: %s} }",
                r, mErrorResponse == null ? tryGetResponseStr(mResponse)
                        : mErrorResponse
        );
    }
}
