package io.complicated.stereostream;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.lang.ref.WeakReference;
import java.util.Locale;

import io.complicated.stereostream.api.room.Room;
import io.complicated.stereostream.api.room.RoomClient;
import io.complicated.stereostream.utils.ActivityUtilsSingleton;
import io.complicated.stereostream.utils.CommonErrorHandlerRedirector;
import io.complicated.stereostream.utils.ErrorOrEntity;
import io.complicated.stereostream.utils.PrefSingleton;
import io.complicated.stereostream.utils.ProgressHandler;

import static io.complicated.stereostream.utils.Formatters.ExceptionFormatter;

public final class RoomActivity extends AppCompatActivity {
    private final PrefSingleton mSharedPrefs = PrefSingleton.getInstance();
    private final ActivityUtilsSingleton mUtils = ActivityUtilsSingleton.getInstance();
    private ProgressBar mProgressView;
    private ViewSwitcher mViewSwitcher;
    private TextView mReadRoomName;
    private TextView mReadRoomEmail;
    private TextInputEditText mEditRoomName;
    private AutoCompleteTextView mEditRoomEmail;
    private Button mEditRoomButton;
    private TextView mErrorView;
    private CommonErrorHandlerRedirector mCommonErrorHandlerRedirector;
    private static RoomClient mRoomClient;
    private ProgressHandler mProgressHandler;
    private UpdateRoomTask mUpdateRoomsTask = null;
    private DeleteRoomTask mDeleteRoomsTask = null;
    private String mOwner;

    private void showReadView() {
        if (isUpdateView()) mViewSwitcher.setDisplayedChild(0);
    }

    private void showUpdateView() {
        if (!isUpdateView()) mViewSwitcher.setDisplayedChild(1);
    }

    private boolean isUpdateView() {
        return mViewSwitcher.getDisplayedChild() == 1;
    }

    @Override
    public final void onBackPressed() {
        if (isUpdateView()) {
            showReadView();
            return;
        }
        finish();
        super.onBackPressed();
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        mSharedPrefs.Init(getApplicationContext());
        mUtils.Init(savedInstanceState, getIntent(), mSharedPrefs);

        final String accessToken = mUtils.getFromLocalOrCache("access_token");
        if (accessToken == null) {
            startActivity(new Intent(this, AuthActivity.class));
            return;
        }

        mRoomClient = new RoomClient(this, accessToken);
        final Room room = Room.fromString(mUtils.getFromLocalOrCache("current_room"));
        if (room.getOwner() == null)
            room.setOwner(mUtils.getFromLocalOrCache("email"));

        mCommonErrorHandlerRedirector = new CommonErrorHandlerRedirector(this, mSharedPrefs);

        mViewSwitcher = (ViewSwitcher) findViewById(R.id.activity_room_view_switcher);
        showReadView();

        mReadRoomName = (TextView) findViewById(R.id.activity_room_item_name);

        mEditRoomName = (TextInputEditText) findViewById(R.id.activity_room_update_name);
        mEditRoomButton = (Button) findViewById(R.id.activity_room_update_button);
        if (mEditRoomButton != null)
            mEditRoomButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public final void onClick(final View view) {
                    showProgress(true);
                    final Room newRoom = (Room) room.clone();
                    newRoom.setName(mEditRoomName.getText().toString());
                    mUpdateRoomsTask = new UpdateRoomTask(RoomActivity.this, room, newRoom);
                    mUpdateRoomsTask.execute((Void) null);
                }
            });

        final FloatingActionButton roomEditBtn = (FloatingActionButton) findViewById(R.id.activity_room_item_owner_edit_btn);
        if (roomEditBtn != null) roomEditBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        showUpdateView();
                    }
                });

        final FloatingActionButton roomDelBtn = (FloatingActionButton) findViewById(R.id.activity_room_item_owner_del_btn);
        if (roomDelBtn != null) roomDelBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        mDeleteRoomsTask = new DeleteRoomTask(RoomActivity.this, room);
                        mDeleteRoomsTask.execute((Void) null);
                    }
                });

        mProgressView = (ProgressBar)findViewById(R.id.activity_room_update_progress);
        mErrorView = (TextView) findViewById(R.id.activity_room_update_errors);

        mProgressHandler = new ProgressHandler(mProgressView, mViewSwitcher,
                getResources().getInteger(android.R.integer.config_shortAnimTime));

        setRoomInView(room);
        setEditRoomInView(room);
    }

    private void setRoomInView(@NonNull final Room room) {
        mReadRoomName.setText(room.getName());
    }

    private void setEditRoomInView(@NonNull final Room room) {
        mEditRoomName.setText(room.getName());
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        mProgressHandler.showProgress(show);
    }

    public static final class UpdateRoomTask extends AsyncTask<Void, Void,
            ErrorOrEntity<Room>> {

        private final Room mPrevRoom, mNewRoom;
        private final WeakReference<RoomActivity> mWeakActivity;

        UpdateRoomTask(final RoomActivity activity, final Room room,
                          final Room newRoom) {
            mPrevRoom = room;
            mNewRoom = newRoom;
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        protected final ErrorOrEntity<Room> doInBackground(final Void... params) {
            if (mNewRoom.getOwner() == null)
                mNewRoom.setOwner(mPrevRoom.getOwner());
            return mRoomClient.putSync(mPrevRoom, mNewRoom);
        }

        @Override
        protected final void onPostExecute(final ErrorOrEntity<Room> err_res) {
            final RoomActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mUpdateRoomsTask = null;
            activity.showProgress(false);

            if (err_res.success()) {
                activity.finish();
                final Intent intent = new Intent(activity.getApplicationContext(),
                        RoomsActivity.class);
                intent.putExtra("new_room", err_res.getEntity().toString());
                activity.startActivity(intent);
            } else {
                activity.mCommonErrorHandlerRedirector.process(err_res);
                if (err_res.getErrorResponse() == null) {
                    err_res.getException().printStackTrace(System.err);
                    activity.mErrorView.setText(ExceptionFormatter(err_res.getException()));
                } else activity.mErrorView.setText(String.format(Locale.getDefault(),
                        "[%d] %s", err_res.getCode(), err_res.getErrorResponse().toString()));
            }
        }

        @Override
        protected final void onCancelled() {
            final RoomActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mUpdateRoomsTask = null;
            activity.showProgress(false);
        }
    }

    public static final class DeleteRoomTask extends AsyncTask<Void, Void,
            ErrorOrEntity<Room>> {

        private final Room mRoom;
        private final WeakReference<RoomActivity> mWeakActivity;

        DeleteRoomTask(final RoomActivity activity, final Room room) {
            mRoom = room;
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        protected final ErrorOrEntity<Room> doInBackground(final Void... params) {
            return mRoomClient.delSync(mRoom);
        }

        @Override
        protected final void onPostExecute(final ErrorOrEntity<Room> err_res) {
            final RoomActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mDeleteRoomsTask = null;
            activity.showProgress(false);

            if (err_res.success()) {
                activity.finish();
                activity.startActivity(new Intent(activity.getApplicationContext(),
                        RoomsActivity.class));
            } else {
                activity.mCommonErrorHandlerRedirector.process(err_res);
                if (err_res.getErrorResponse() == null) {
                    err_res.getException().printStackTrace(System.err);
                    activity.mErrorView.setText(ExceptionFormatter(err_res.getException()));
                } else activity.mErrorView.setText(String.format(Locale.getDefault(),
                        "[%d] %s", err_res.getCode(), err_res.getErrorResponse().toString()));
            }
        }

        @Override
        protected final void onCancelled() {
            final RoomActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mUpdateRoomsTask = null;
            activity.showProgress(false);
        }
    }
}
