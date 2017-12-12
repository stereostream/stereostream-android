package io.complicated.stereostream;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.ViewSwitcher;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.Locale;

import io.complicated.stereostream.api.room.Room;
import io.complicated.stereostream.api.room.RoomClient;
import io.complicated.stereostream.api.room.RoomWithLog;
import io.complicated.stereostream.utils.ActivityUtilsSingleton;
import io.complicated.stereostream.utils.ChatClient;
import io.complicated.stereostream.utils.CommonErrorHandlerRedirector;
import io.complicated.stereostream.utils.ErrorHandler;
import io.complicated.stereostream.utils.ErrorOrEntity;
import io.complicated.stereostream.utils.PrefSingleton;
import io.complicated.stereostream.utils.ProgressHandler;

import static io.complicated.stereostream.utils.Formatters.ExceptionFormatter;

public final class RoomActivity extends AppCompatActivity {
    private final PrefSingleton mSharedPrefs = PrefSingleton.getInstance();
    private final ActivityUtilsSingleton mUtils = ActivityUtilsSingleton.getInstance();
    private ProgressBar mProgressView;
    private ViewSwitcher mViewSwitcher;

    private TextInputEditText mEditRoomName;
    private Button mEditRoomButton;
    private TextView mErrorView;
    private CommonErrorHandlerRedirector mCommonErrorHandlerRedirector;
    private static RoomClient mRoomClient;
    private ProgressHandler mProgressHandler;
    private GetRoomTask mGetRoomTask = null;
    private UpdateRoomTask mUpdateRoomsTask = null;
    private DeleteRoomTask mDeleteRoomsTask = null;
    private String mOwner;

    private RoomWithLog mRoomWithLog = null;

    // Chat
    private ScrollView mChatLogScroll;
    private TextView mChatLog;
    private EditText mChatInput;
    private Button mChatInputSend;
    private ChatClient mChatClient;

    // Video
    private VideoView mVideoView0;
    private VideoView mVideoView1;

    private void showReadView() {
        if (isUpdateView()) mViewSwitcher.setDisplayedChild(0);
    }

    private void showUpdateView() {
        if (!isUpdateView()) mViewSwitcher.setDisplayedChild(1);
    }

    private boolean isUpdateView() {
        return mViewSwitcher.getDisplayedChild() == 1;
    }

    public final void setRoomWithLog(final RoomWithLog roomWithLog) {
        mRoomWithLog = roomWithLog;
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

        try {
            mRoomClient = new RoomClient(this, accessToken);
        } catch (RuntimeException | ConnectException e) {
            ErrorHandler.askCloseApp(this, e.getMessage(), mSharedPrefs);
            return;
        }
        final Room room = Room.fromString(mUtils.getFromLocalOrCache("current_room"));
        if (room.getOwner().length() > 0)
            room.setOwner(mUtils.getFromLocalOrCache("email"));

        setTitle(room.getName());

        final ActionBar toolbar = getSupportActionBar();
        if (toolbar != null)
            toolbar.setDisplayHomeAsUpEnabled(true);

        mCommonErrorHandlerRedirector = new CommonErrorHandlerRedirector(this, mSharedPrefs);

        mViewSwitcher = (ViewSwitcher) findViewById(R.id.activity_room_view_switcher);
        showReadView();

        if (mRoomWithLog == null) {
            mGetRoomTask = new GetRoomTask(RoomActivity.this, room);
            mGetRoomTask.execute((Void) null);
        }

        mRoomWithLog = RoomWithLog.fromString(mUtils.getFromLocalOrCache("room_with_log"));

        /*
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
        */

        mProgressView = (ProgressBar) findViewById(R.id.activity_room_update_progress);
        mErrorView = (TextView) findViewById(R.id.activity_room_update_errors);

        mProgressHandler = new ProgressHandler(mProgressView, mViewSwitcher,
                getResources().getInteger(android.R.integer.config_shortAnimTime));

        // Text
        try {
            mChatClient = new ChatClient(mRoomClient.getBaseUri() + "/socket.io");
            mChatClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace(System.err);
            Log.getStackTraceString(e);
        }

        mChatLogScroll = (ScrollView) findViewById(R.id.activity_room_chat_log_scroll);
        mChatLog = (TextView) findViewById(R.id.activity_room_chat_log);
        mChatLog.setText(mRoomWithLog == null ? "" : mRoomWithLog.getLogStr());

        mChatInput = (EditText) findViewById(R.id.activity_room_chat_input);
        mChatInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public final boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    mChatInputSend.performClick();
                    return true;
                }
                return false;
            }
        });

        mChatInputSend = (Button) findViewById(R.id.activity_room_chat_input_send);

        mChatInputSend.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View v) {
                final String msg = String.format(
                        Locale.getDefault(), "%s", mChatInput.getText()
                );
                if (msg != null && msg.length() > 0) {
                    mChatClient.connect();
                    mChatClient.send(String.format(Locale.getDefault(), "%s\t%s\t%s",
                            accessToken, room.getName(), msg
                    ));
                    mChatLog.setText(String.format(Locale.getDefault(), "%s\n%s",
                            msg, mChatLog.getText()
                    ));
                }
                mChatInput.setText(null);
            }
        });
    }

    @Override
    protected final void onStart() {
        super.onStart();

        mVideoView0 = (VideoView) findViewById(R.id.video_view0);
        // mVideoView0.setOnPreparedListener(RoomActivity.this);

        //For now we just picked an arbitrary item to play
        mVideoView0.setVideoURI(Uri.parse(String.format(Locale.getDefault(), "%s:8085/stream0.webm", mRoomClient.getNonApiBaseUri())));
        mVideoView0.start();

        mVideoView1 = (VideoView) findViewById(R.id.video_view1);
        mVideoView1.setVideoURI(Uri.parse(String.format(Locale.getDefault(), "%s:8086/stream1.webm", mRoomClient.getNonApiBaseUri())));
        mVideoView1.start();
        // setEditRoomInView(room);
    }

    @Override
    protected final void onPause() {
        super.onPause();

        if (mVideoView0 != null && mVideoView0.canPause())
            mVideoView0.pause();

        if (mVideoView1 != null && mVideoView1.canPause())
            mVideoView1.pause();
    }

    @Override
    protected final void onResume() {
        super.onResume();

        if (mVideoView0 != null && !mVideoView0.isPlaying())
            mVideoView0.resume();

        if (mVideoView1 != null && mVideoView1.isPlaying())
            mVideoView1.resume();
    }

    private void setEditRoomInView(@NonNull final Room room) {
        mEditRoomName.setText(room.getName());
    }

    /**
     * Shows the progress UI and hides the room form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        mProgressHandler.showProgress(show);
    }

    public static final class GetRoomTask extends AsyncTask<Void, Void, ErrorOrEntity<RoomWithLog>> {
        private final Room mRoom;
        private final WeakReference<RoomActivity> mWeakActivity;

        GetRoomTask(final RoomActivity activity, final Room room) {
            mRoom = room;
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        protected final ErrorOrEntity<RoomWithLog> doInBackground(final Void... params) {
            return mRoomClient.getSync(mRoom);
        }

        @Override
        protected final void onPostExecute(final ErrorOrEntity<RoomWithLog> err_res) {
            final RoomActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mGetRoomTask = null;
            activity.showProgress(false);

            if (err_res.success()) {
                /*
                activity.finish();

                final Intent intent = new Intent(activity.getApplicationContext(),
                        RoomsActivity.class);
                intent.putExtra("room_with_log", err_res.getEntity().toString());
                activity.startActivity(intent);
                */
                activity.setRoomWithLog(err_res.getEntity());
                activity.mChatLog.setText(activity.mRoomWithLog.getLogStr());
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

            activity.mGetRoomTask = null;
            activity.showProgress(false);
        }
    }

    public static final class UpdateRoomTask extends AsyncTask<Void, Void, ErrorOrEntity<Room>> {
        private final Room mPrevRoom, mNewRoom;
        private final WeakReference<RoomActivity> mWeakActivity;

        UpdateRoomTask(final RoomActivity activity, final Room room, final Room newRoom) {
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

    public static final class DeleteRoomTask extends AsyncTask<Void, Void, ErrorOrEntity<Room>> {
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
