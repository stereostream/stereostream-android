package io.complicated.stereostream;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import io.complicated.stereostream.api.room.Room;
import io.complicated.stereostream.api.room.RoomAdapter;
import io.complicated.stereostream.api.room.ListRooms;
import io.complicated.stereostream.utils.CommonErrorHandlerRedirector;
import io.complicated.stereostream.utils.ErrorHandler;
import io.complicated.stereostream.utils.ErrorOrEntity;
import io.complicated.stereostream.utils.Formatters;
import io.complicated.stereostream.utils.PrefSingleton;
import io.complicated.stereostream.api.room.RoomsClient;
import io.complicated.stereostream.utils.ActivityUtilsSingleton;
import io.complicated.stereostream.utils.ProgressHandler;

public final class RoomsActivity extends AppCompatActivity {
    private final PrefSingleton mSharedPrefs = PrefSingleton.getInstance();
    private final ActivityUtilsSingleton mUtils = ActivityUtilsSingleton.getInstance();
    private static RoomsClient mRoomsClient;
    private LoadRoomsTask mLoadRoomsTask = null;
    private View mProgressView;
    private TextView mInfoMsg;
    private ViewSwitcher mContentViewSwitcher;
    private ListView mContentListView;
    private CommonErrorHandlerRedirector mCommonErrorHandlerRedirector;
    private ProgressHandler mProgressHandler;
    public static final int CREATE = 0; // enum
    private String mAccessToken;
    //private View mRoomItemView;
    //private TextView getError()View;

    private void showEmptyRoomsView() {
        mContentViewSwitcher.setDisplayedChild(0);
    }

    private void showListOfRoomsView() {
        mContentViewSwitcher.setDisplayedChild(1);
    }

    @Override
    protected final void onStart() {
        super.onStart();
        Log.d("lifecycle", "onStart");
        try {
            mRoomsClient = new RoomsClient(this, mAccessToken);
        } catch (RuntimeException | ConnectException e) {
            ErrorHandler.askCloseApp(this, e.getMessage(), mSharedPrefs);
            return;
        }

        mProgressView = findViewById(R.id.rooms_progress);

        mInfoMsg = (TextView) findViewById(R.id.rooms_info_msg);
        mContentViewSwitcher = (ViewSwitcher) findViewById(R.id.rooms_content_view_switcher);
        mContentListView = (ListView) findViewById(R.id.rooms_list);
        mContentListView.setOnItemClickListener(new ItemClick());
        mCommonErrorHandlerRedirector = new CommonErrorHandlerRedirector(this, mSharedPrefs);
        mProgressHandler = new ProgressHandler(mProgressView, mContentViewSwitcher,
                getResources().getInteger(android.R.integer.config_shortAnimTime));
        //getError()View = (TextView) findViewById(R.id.errors);

        final Room newRoom = Room.fromString(mUtils.getFromLocalOrCache("new_room"));
        loadRooms(newRoom);
    }

    @Override
    protected final void onDestroy() {
        super.onDestroy();
        Log.d("lifecycle", "onDestroy");
        if (mContentListView != null && mContentListView.getAdapter() != null)
            Log.d("onDestroy::list", String.valueOf(mContentListView.getAdapter().getCount()));
    }

    @Override
    protected final void onResume() {
        super.onResume();
        Log.d("lifecycle", "onResume");
        if (mContentListView != null && mContentListView.getAdapter() != null)
            Log.d("onResume::list", String.valueOf(mContentListView.getAdapter().getCount()));
        if (mAccessToken == null)
            try {
                mAccessToken = getIntent().getExtras().getString("access_token");
            } catch (NullPointerException e) {
                mAccessToken = null;
            }
    }

    @Override
    protected final void onPause() {
        super.onPause();
        Log.d("lifecycle", "onPause");
        if (mContentListView != null && mContentListView.getAdapter() != null)
            Log.d("onPause::list", String.valueOf(mContentListView.getAdapter().getCount()));
    }

    @Override
    protected final void onRestart() {
        super.onRestart();
        Log.d("lifecycle", "onRestart");
        if (mContentListView != null && mContentListView.getAdapter() != null)
            Log.d("onRestart::list", String.valueOf(mContentListView.getAdapter().getCount()));
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rooms);
        Log.d("lifecycle", "onCreate");

        mSharedPrefs.Init(getApplicationContext());
        mUtils.Init(savedInstanceState, getIntent(), mSharedPrefs);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.rooms_toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton add_btn = (FloatingActionButton) findViewById(R.id.rooms_add_btn);
        if (add_btn != null)
            add_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    startActivity(new Intent(RoomsActivity.this, CreateRoomActivity.class));
                }
            });

        mAccessToken = mUtils.getFromLocalOrCache("access_token");

        if (mAccessToken == null) {
            Log.e("RoomsActivity", "access_token is null; redirecting...");
            startActivityForResult(new Intent(this, AuthActivity.class), CREATE);
        }
    }

    final class ItemClick implements AdapterView.OnItemClickListener {
        public final void onItemClick(final AdapterView<?> parent, final View view,
                                      final int position, final long id) {
            final Intent intent = new Intent(getApplicationContext(), RoomActivity.class);
            intent.putExtra("current_room",
                    mContentListView.getItemAtPosition(position).toString());
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        // Check which request we're responding to
        Log.d("onActivityResult",
                String.format(Locale.getDefault(), "requestCode = %d, resultCode = %d, data = %s", data));
        if (requestCode == CREATE && resultCode == RESULT_OK) {
            Log.d("onActivityResult",
                    String.format(Locale.getDefault(), "data = %s", data.getExtras().getString("new_room")));
        }
    }

    @Override
    public final void onBackPressed() {
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        mProgressHandler.showProgress(show);
    }

    private void loadRooms(final Room newRoom) {
        // if (mLoadRoomsTask != null) return;

        Log.d("loadRooms", "Got here");

        showProgress(true);
        final String s = null; // mUtils.getFromLocalOrCache("listRooms");
        mLoadRoomsTask = new LoadRoomsTask(this, s == null ? null : ListRooms.fromString(s),
                newRoom);
        mLoadRoomsTask.execute((Void) null);
    }

    /**
     * Represents an asynchronous task
     */
    public static final class LoadRoomsTask extends AsyncTask<Void, Void, ErrorOrEntity<ListRooms>> {
        final Room mNewRoom;
        private final WeakReference<RoomsActivity> mWeakActivity;
        private final ListRooms mListRooms;

        LoadRoomsTask(final RoomsActivity activity,
                      final ListRooms listRooms,
                      final Room newRoom) {
            mNewRoom = newRoom;
            mWeakActivity = new WeakReference<>(activity);
            mListRooms = listRooms;
        }

        @Override
        protected final ErrorOrEntity<ListRooms> doInBackground(final Void... params) {
            if (mListRooms != null && mListRooms.getRooms().length > 0) {
                final ErrorOrEntity<ListRooms> err_res = new ErrorOrEntity<>(
                        null, null, mListRooms
                );
                onPostExecute(err_res);
                return null;
            } else return mRoomsClient.getSync();
        }

        @Override
        protected final void onPostExecute(final ErrorOrEntity<ListRooms> err_res) {
            final RoomsActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mLoadRoomsTask = null;
            activity.showProgress(false);

            Log.d("onPostExecute", String.valueOf(err_res.getCode()));
            Log.d("err_res.success()", String.valueOf(err_res.success()));

            Log.d("0_getDisplayedChild()", String.valueOf(activity.mContentViewSwitcher.getDisplayedChild()));

            if (err_res.success()) {
                final Room[] rooms = err_res.getEntity().getRooms();
                activity.mSharedPrefs.putString("listRooms", Arrays.toString(rooms));
                final RoomAdapter roomAdapter = new RoomAdapter(
                        activity.getApplicationContext(), rooms
                );
                if (roomAdapter.getCount() > 0) {
                    activity.mContentListView.setAdapter(roomAdapter);
                    ((BaseAdapter) activity.mContentListView.getAdapter()).notifyDataSetChanged();
                    //rooms.clone();
                    Log.d("onPostExecute::listsize", String.valueOf(activity.mContentListView.getAdapter().getCount()));
                    activity.showListOfRoomsView();
                    Log.d("1_getDisplayedChild()", String.valueOf(activity.mContentViewSwitcher.getDisplayedChild()));
                } else {
                    activity.mInfoMsg.setText(activity.getString(R.string.empty_rooms));
                    activity.showEmptyRoomsView();
                    Log.d("2_getDisplayedChild()", String.valueOf(activity.mContentViewSwitcher.getDisplayedChild()));
                }
            } else {
                Log.d("onPostExecute::else", "");
                activity.mCommonErrorHandlerRedirector.process(err_res);
                Log.d("onPostExecute::else", "after activity.mCommonErrorHandlerRedirector.process(err_res)");
                activity.showEmptyRoomsView();
                Log.d("3_getDisplayedChild()", String.valueOf(activity.mContentViewSwitcher.getDisplayedChild()));
                Log.d("onPostExecute::else", "after activity.showEmptyRoomsView()");
                if (err_res.getErrorResponse() == null) {
                    Log.d("onPostExecute::else", "err_res.getErrorResponse() == null");
                    err_res.getException().printStackTrace(System.err);
                    activity.mInfoMsg.setTextColor(Color.RED);
                    activity.mInfoMsg.setText(Formatters.ExceptionFormatter(err_res.getException()));
                } else {
                    Log.d("onPostExecute::else", "else");
                    activity.mInfoMsg.setText(err_res.getCode() == 404 ?
                            activity.getString(R.string.empty_rooms) :
                            err_res.getErrorResponse().toString());
                }
            }
        }

        @Override
        protected final void onCancelled() {
            final RoomsActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mLoadRoomsTask = null;
            activity.showProgress(false);
        }
    }
}
