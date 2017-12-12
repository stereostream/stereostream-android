package io.complicated.stereostream.utils;

import android.content.Context;
import android.util.Log;

import java.net.ConnectException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.complicated.stereostream.R;
import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;


public class BaseApiClient {
    private final OkHttpClient mClient;
    private final String mBaseUri;

    public BaseApiClient(final Context context,
                         final String hostname,
                         final CachedReq cache,
                         final Authenticator authenticator,
                         final Interceptor[] interceptors,
                         final boolean authInterceptor,
                         final String accessToken) throws ConnectException {
        final OkHttpClient.Builder builder = cache == null ?
                (new CachedReq(context.getFilesDir().getPath(),
                        getClass().getCanonicalName())
                ).getClientBuilder() : cache.getClientBuilder();
        if (authenticator != null)
            builder.authenticator(authenticator);
        if (interceptors != null && interceptors.length > 0)
            for (final Interceptor interceptor : interceptors)
                builder.addInterceptor(interceptor);
        if (authInterceptor && accessToken != null)
            builder.addInterceptor(new AuthRequestInterceptor(accessToken));
        mClient = builder.build();
        final String s = String.format("%s/api", hostname == null ?
                (PrefSingleton.getInstance().contains("API") ?
                        PrefSingleton.getInstance().getString("API")
                        : context.getString(R.string.api_prefix)) : hostname);
        mBaseUri = s.startsWith("http") ? s : String.format("http://%s", s);
        if (!NetworkUtils.isNetworkAvailable(context))
            throw new ConnectException("Network not connected");
        else {
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            final Runnable task = new Runnable() {
                public void run() {
                    if (!NetworkUtils.get(mBaseUri))
                        throw new RuntimeException("API not connected");
                }
            };

            final Future<?> future = executor.submit(task);
            try {
                future.get();
            } catch (ExecutionException e) {
                e.printStackTrace(System.err);
                throw new ConnectException(e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
                throw new ConnectException(e.getMessage());
            }
        }
    }

    public BaseApiClient(final Context context,
                         final String hostname,
                         final CachedReq cache) throws ConnectException {
        this(context, hostname, cache,
                null, null, false,
                null);
    }

    public final OkHttpClient getClient() {
        return mClient;
    }

    public final String getBaseUri() {
        return mBaseUri;
    }
}
