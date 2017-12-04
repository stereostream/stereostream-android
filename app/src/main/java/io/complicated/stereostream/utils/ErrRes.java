package io.complicated.stereostream.utils;

import java.util.Locale;

import android.util.Pair;


/**
 * ErrRes class
 */
public class ErrRes<F, S> extends Pair<F, S> {
    private F mFallbackError;

    public ErrRes(final F error, final S result) {
        super(error, result);
    }

    public final boolean failure() {
        return !success();
    }

    public final boolean success() {
        return getError() == null && getResult() != null;
    }

    public final F getError() {
        return first == null ? mFallbackError : first;
    }

    public final void setFallbackError(final F newError) {
        mFallbackError = newError;
    }

    public final S getResult() {
        return second;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "ErrRes{error: %s, res: %s}",
                getError(), getResult());
    }
}
