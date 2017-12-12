package io.complicated.stereostream;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.util.Locale;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import io.complicated.stereostream.api.room.Room;
import io.complicated.stereostream.utils.CommonErrorHandlerRedirector;
import io.complicated.stereostream.utils.ErrorHandler;
import io.complicated.stereostream.utils.ErrorOrEntity;
import io.complicated.stereostream.utils.Formatters;
import io.complicated.stereostream.utils.PrefSingleton;
import io.complicated.stereostream.api.room.RoomsClient;
import io.complicated.stereostream.utils.ActivityUtilsSingleton;
import io.complicated.stereostream.utils.ProgressHandler;

/*implements LoaderCallbacks<Cursor>*/
public final class CreateRoomActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private CreateRoomTask mCreateRoomTask = null;

    // UI references.
    private TextView mNameView;
    private View mProgressView;
    private View mCreateRoomFormView;
    private TextView mErrorView;
    private CommonErrorHandlerRedirector mCommonErrorHandlerRedirector;
    private ProgressHandler mProgressHandler;
    private final PrefSingleton mSharedPrefs = PrefSingleton.getInstance();
    private final ActivityUtilsSingleton mUtils = ActivityUtilsSingleton.getInstance();
    private static RoomsClient mRoomsClient;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        mSharedPrefs.Init(getApplicationContext());
        mUtils.Init(savedInstanceState, getIntent(), mSharedPrefs);

        final String accessToken = mUtils.getFromLocalOrCache("access_token");
        if (accessToken == null) {
            startActivity(new Intent(this, AuthActivity.class));
            return;
        }

        final ActionBar toolbar = getSupportActionBar();
        try {
            toolbar.setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace(System.err);
        }

        // Set up the form.
        mErrorView = (TextView) findViewById(R.id.create_room_errors);
        mNameView = (TextView) findViewById(R.id.create_room_name);

        final Button mCreateRoomButton = (Button) findViewById(R.id.create_room_button);
        if (mCreateRoomButton != null)
            mCreateRoomButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View view) {
                    attemptCreate();
                }
            });

        mCreateRoomFormView = findViewById(R.id.create_room_form);
        mProgressView = findViewById(R.id.create_room_progress);
        mProgressHandler = new ProgressHandler(mProgressView, mCreateRoomFormView,
                getResources().getInteger(android.R.integer.config_shortAnimTime)
        );

        try {
            mRoomsClient = new RoomsClient(this, accessToken);
        } catch (RuntimeException | ConnectException e) {
            ErrorHandler.askCloseApp(this, e.getMessage(), mSharedPrefs);
            return;
        }

        mCommonErrorHandlerRedirector = new CommonErrorHandlerRedirector(this, mSharedPrefs);
    }

    @Override
    public final void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    private void attemptCreate() {
        if (mCreateRoomTask != null)
            return;

        // Reset errors.
        mNameView.setError(null);

        // Store values at the time of the login attempt.
        final String name = mNameView.getText().toString();

        showProgress(true);
        mCreateRoomTask = new CreateRoomTask(this, new Room(name));
        mCreateRoomTask.execute((Void) null);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        mProgressHandler.showProgress(show);
    }

    /*
    @Override
    public final Loader<Cursor> onCreateLoader(final int i, final Bundle bundle) {
    }

    @Override
    public final void onLoadFinished(final Loader<Cursor> cursorLoader, final Cursor cursor) {
    }

    @Override
    public final void onLoaderReset(final Loader<Cursor> cursorLoader) {
    }
    */

    public final static class CreateRoomTask extends AsyncTask<Void, Void, ErrorOrEntity<Room>> {
        private final Room mRoom;
        private final WeakReference<CreateRoomActivity> mWeakActivity;

        CreateRoomTask(final CreateRoomActivity activity, final Room room) {
            mRoom = room;
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        protected final ErrorOrEntity<Room> doInBackground(final Void... params) {
            return mRoomsClient.postSync(mRoom);
        }

        @Override
        protected final void onPostExecute(final ErrorOrEntity<Room> err_res) {
            final CreateRoomActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mCreateRoomTask = null;
            activity.showProgress(false);

            if (err_res.success()) {
                activity.finish();
                final Intent intent = new Intent(
                        activity.getApplicationContext(), RoomsActivity.class
                );
                intent.putExtra("new_room", mRoom.toString());
                activity.setResult(RESULT_OK, intent);
                activity.finish();
            } else {
                activity.mCommonErrorHandlerRedirector.process(err_res);

                if (err_res.getErrorResponse() == null) {
                    err_res.getException().printStackTrace(System.err);
                    activity.mErrorView.setText(Formatters.ExceptionFormatter(err_res.getException()));
                } else activity.mErrorView.setText(String.format(Locale.getDefault(),
                        "[%d] %s", err_res.getCode(), err_res.getErrorResponse().toString()));
            }
        }

        @Override
        protected final void onCancelled() {
            final CreateRoomActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mCreateRoomTask = null;
            activity.showProgress(false);

            activity.setResult(RESULT_CANCELED, new Intent());
            activity.finish();
        }
    }
}
