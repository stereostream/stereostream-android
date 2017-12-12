package io.complicated.stereostream.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import java.util.Locale;


public class ErrorHandler {
    public static void askCloseApp(final Context context,
                                   final String errorMessage,
                                   final PrefSingleton sharedPrefs) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setTextColor(Color.GREEN);

        String hintText = "Edit API server";
        if (sharedPrefs.contains("API"))
            hintText = String.format(Locale.getDefault(),
                    "%s\t\t[%s]", hintText,
                    sharedPrefs.getString("API")
            );
        input.setHint(hintText);
        input.setHintTextColor(Color.GRAY);

        builder.setView(input);
        builder.setTitle(errorMessage == null ? "Cannot connect" : errorMessage)
                .setMessage("Check network connectivity and confirm API server is running.\nThen relaunch.")
                .setPositiveButton("Close app", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String inputS = input.getText().toString();
                        if (inputS.length() > 1)
                            sharedPrefs.putString("API", inputS);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
