package io.complicated.stereostream.utils;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class HierarchicalViewIdViewer {
    public static View debugViewIds(final View view, final String logtag) {
        Log.v(logtag, "traversing: " + view.getClass().getSimpleName() + ", id: " + view.getId());
        if (view.getParent() != null && (view.getParent() instanceof ViewGroup)) {
            return debugViewIds((View) view.getParent(), logtag);
        } else {
            debugChildViewIds(view, logtag, 0);
            return view;
        }
    }

    private static void debugChildViewIds(final View view, final String logtag, final int spaces) {
        if (view instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                final View child = group.getChildAt(i);
                Log.v(logtag, padString("view: " + child.getClass().getSimpleName() + "(" + child.getId() + ")", spaces));
                debugChildViewIds(child, logtag, spaces + 1);
            }
        }
    }

    private static String padString(final String str, final int noOfSpaces) {
        if (noOfSpaces <= 0) {
            return str;
        }
        final StringBuilder builder = new StringBuilder(str.length() + noOfSpaces);
        for (int i = 0; i < noOfSpaces; i++) {
            builder.append(' ');
        }
        return builder.append(str).toString();
    }

}
