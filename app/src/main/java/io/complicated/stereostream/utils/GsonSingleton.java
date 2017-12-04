package io.complicated.stereostream.utils;

import com.google.gson.Gson;

public class GsonSingleton {
    private static GsonSingleton mInstance;
    private static Gson mGson;

    private GsonSingleton() {
    }

    public static synchronized Gson getGson() {
        if (mGson == null) mGson = new Gson();
        return mGson;
    }

    public final void Init() {
        mGson = getGson();
    }
}
