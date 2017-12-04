package io.complicated.stereostream.utils;


public class Formatters {
    public static String ExceptionFormatter(final Exception e) {
        return e.getClass().getCanonicalName() + ": " +  e.getMessage();
    }
}
