package io.complicated.stereostream;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.Locale;

import io.complicated.stereostream.api.room.ListRooms;
import io.complicated.stereostream.api.room.Room;
import io.complicated.stereostream.api.room.RoomAdapter;
import io.complicated.stereostream.api.room.RoomsClient;
import io.complicated.stereostream.utils.ActivityUtilsSingleton;
import io.complicated.stereostream.utils.CommonErrorHandlerRedirector;
import io.complicated.stereostream.utils.ErrorHandler;
import io.complicated.stereostream.utils.ErrorOrEntity;
import io.complicated.stereostream.utils.Formatters;
import io.complicated.stereostream.utils.PrefSingleton;
import io.complicated.stereostream.utils.ProgressHandler;

import static io.complicated.stereostream.utils.GsonSingleton.getGson;


public final class RoomsActivity extends AppCompatActivity {
    public static final int CREATE = 0; // enum
    private static RoomsClient mRoomsClient;
    private final PrefSingleton mSharedPrefs = PrefSingleton.getInstance();
    private final ActivityUtilsSingleton mUtils = ActivityUtilsSingleton.getInstance();
    private LoadRoomsTask mLoadRoomsTask = null;
    private View mProgressView;
    private TextView mInfoMsg;
    private ViewSwitcher mContentViewSwitcher;
    private ListView mContentListView;
    private CommonErrorHandlerRedirector mCommonErrorHandlerRedirector;
    private ProgressHandler mProgressHandler;
    private String mAccessToken;
    private Toolbar mToolbar;
    private FloatingActionButton mAddRoomBtn;
    //private View mRoomItemView;
    //private TextView getError()View;

    private static void setRoomsAdapter(final RoomsActivity activity,
                                        final Room[] rooms) {
        final RoomAdapter roomAdapter = new RoomAdapter(
                activity.getApplicationContext(), rooms
        );
        Log.d("setRoomsAdapter", Integer.toString(roomAdapter.getCount()));
        if (roomAdapter.getCount() > 0) {
            activity.mContentListView.setAdapter(roomAdapter);
            ((BaseAdapter) activity.mContentListView.getAdapter()).notifyDataSetChanged();
            Log.d("0shown", Boolean.toString(activity.mContentListView.isShown()));
            //rooms.clone();
            Log.d("onPostExecute::listsize", String.valueOf(activity.mContentListView.getAdapter().getCount()));
            activity.showListOfRoomsView();
            Log.d("1_getDisplayedChild()", String.valueOf(activity.mContentViewSwitcher.getDisplayedChild()));
            Log.d("1shown", Boolean.toString(activity.mContentListView.isShown()));
        } else {
            activity.mInfoMsg.setText(activity.getString(R.string.empty_rooms));
            activity.showEmptyRoomsView();
            Log.d("2_getDisplayedChild()", String.valueOf(activity.mContentViewSwitcher.getDisplayedChild()));
        }
    }

    private void showEmptyRoomsView() {
        mContentViewSwitcher.setDisplayedChild(0);
    }

    private void showListOfRoomsView() {
        mContentViewSwitcher.setDisplayedChild(1);
    }

    private void bindView() {
        if (mProgressView == null)
            mProgressView = findViewById(R.id.rooms_progress);
        if (mInfoMsg == null)
            mInfoMsg = (TextView) findViewById(R.id.rooms_info_msg);
        if (mContentViewSwitcher == null)
            mContentViewSwitcher = (ViewSwitcher) findViewById(R.id.rooms_content_view_switcher);
        if (mContentListView == null) {
            mContentListView = (ListView) findViewById(R.id.rooms_list);
            mContentListView.setOnItemClickListener(new ItemClick());
        }
        if (mCommonErrorHandlerRedirector == null)
            mCommonErrorHandlerRedirector = new CommonErrorHandlerRedirector(this, mSharedPrefs);
        if (mProgressHandler == null)
            mProgressHandler = new ProgressHandler(mProgressView, mContentViewSwitcher,
                    getResources().getInteger(android.R.integer.config_shortAnimTime));
        //getError()View = (TextView) findViewById(R.id.errors);

        if (mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.rooms_toolbar);
            setSupportActionBar(mToolbar);
        }

        if (mAddRoomBtn == null) {
            mAddRoomBtn = (FloatingActionButton) findViewById(R.id.rooms_add_btn);
            mAddRoomBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    startActivity(new Intent(RoomsActivity.this, CreateRoomActivity.class));
                }
            });
        }
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

        bindView();

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
        bindView();
        if (mContentListView != null && mContentListView.getAdapter() != null)
            Log.d("onResume::list", String.valueOf(mContentListView.getAdapter().getCount()));
        else {
            final Room[] rooms = Room.fromStringArray(mSharedPrefs.getString("listRooms"));
            Log.d("onResume", String.format(Locale.getDefault(),
                    "rooms = %s; length = %d",
                    Arrays.toString(rooms), rooms.length
            ));
            setRoomsAdapter(this, rooms);
        }
        Log.d("onResume::list", String.format(Locale.getDefault(), "mContentListView = %s\nmContentListView.getAdapter() = %s\n(mContentListView.getAdapter() == null) = %s",
                mContentListView, mContentListView.getAdapter(),
                mContentListView.getAdapter() == null
        ));
        if (mAccessToken == null)
            try {
                mAccessToken = getIntent().getExtras().getString("access_token");
            } catch (NullPointerException e) {
                e.printStackTrace(System.err);
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
        bindView();
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

        mAccessToken = mUtils.getFromLocalOrCache("access_token");

        if (mAccessToken == null) {
            Log.e("RoomsActivity", "access_token is null; redirecting...");
            startActivityForResult(new Intent(this, AuthActivity.class), CREATE);
        }

        bindView();
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
                // activity.mSharedPrefs.putString("listRooms", Arrays.toString(rooms));
                activity.mSharedPrefs.putString("listRooms", getGson().toJson(rooms));
                setRoomsAdapter(activity, rooms);
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

    final class ItemClick implements AdapterView.OnItemClickListener {
        public final void onItemClick(final AdapterView<?> parent, final View view,
                                      final int position, final long id) {
            final Intent intent = new Intent(getApplicationContext(), RoomActivity.class);
            intent.putExtra("current_room",
                    mContentListView.getItemAtPosition(position).toString());
            startActivity(intent);
        }
    }
}
