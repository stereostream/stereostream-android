package io.complicated.stereostream.utils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public final class AuthRequestInterceptor implements Interceptor {
    final private String mAccessToken;

    AuthRequestInterceptor(final String accessToken) {
        mAccessToken = accessToken;
    }

    @Override
    public final Response intercept(final Interceptor.Chain chain) throws IOException {
        final Request originalRequest = chain.request();
        final Request compressedRequest = originalRequest.newBuilder()
                .header("X-Access-Token", mAccessToken)
                .method(originalRequest.method(), originalRequest.body())
                .build();
        return chain.proceed(compressedRequest);
    }

    /*private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override public Response intercept(Interceptor.Chain chain) throws IOException {
            final Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                    .body()
                    .build();
        }
    };*/
}
