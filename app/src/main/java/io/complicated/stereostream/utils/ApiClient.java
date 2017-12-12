package io.complicated.stereostream.utils;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;

import io.complicated.stereostream.api.ErrorResponse;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static io.complicated.stereostream.utils.GsonSingleton.getGson;


public final class ApiClient {
    private static Gson mGson = getGson();
    /*
    public static void async(final OkHttpClient client, final Request request) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace(System.err);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                final Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }

                System.out.println(response.body().string());
            }
        });
    }
    */

    public static void async(final OkHttpClient client, final Request request,
                             final Callback callback) {
        client.newCall(request).enqueue(callback);
    }

    public static <T> ErrResResponse<T, ResponseBody> sync(final OkHttpClient client,
                                                           final Request request) throws IOException {
        final Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            final ResponseBody responseBody = response.body();
            // response.close();
            return new ErrResResponse<>(null, responseBody, response);
        }
        // response.close();
        return new ErrResResponse<>(null, null, response);
    }

    public static <E> ErrorOrEntity<E> sync(final OkHttpClient client,
                                            final Request request,
                                            final Class<E> entityClass) {
        final ErrResResponse<Exception, ResponseBody> res;
        try {
            res = sync(client, request);
        } catch (final IOException e) {
            return new ErrorOrEntity<>(e);
        }

        if (Integer.valueOf(res.getResponse().header("Content-Length")) < 1)
            return new ErrorOrEntity<>(new Exception("Empty Content-Length"));
        else if (res.getErrorResponse() != null) {
            return new ErrorOrEntity<>(null, res.getErrorResponse(),
                    res.getResponse().headers(), res.getResponse().code(), null);
        } else if (res.isClosed())
            return new ErrorOrEntity<>(new RuntimeException("Response is already closed"));

        final Reader charStream;
        try {
            charStream = res.getResponse().body().charStream();
        } catch (IllegalStateException | NullPointerException e) {
            return new ErrorOrEntity<>(e);
        }

        try {
            final ErrorOrEntity<E> ret = res.success() ?
                    new ErrorOrEntity<>(res.getError(), null,
                            res.getResponse().headers(), res.getResponse().code(),
                            mGson.fromJson(charStream, entityClass))
                    : new ErrorOrEntity<E>(res.getError(),
                    mGson.fromJson(charStream, ErrorResponse.class),
                    res.getResponse().headers(), res.getResponse().code(), null);
            res.getResponse().close();
            return ret;
        } catch (RuntimeException e) {
            return new ErrorOrEntity<>(e);
        }
    }
}
