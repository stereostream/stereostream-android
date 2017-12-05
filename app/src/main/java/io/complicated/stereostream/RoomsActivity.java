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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.lang.ref.WeakReference;
import java.util.Locale;

import io.complicated.stereostream.api.room.Room;
import io.complicated.stereostream.api.room.RoomAdapter;
import io.complicated.stereostream.api.room.ListRooms;
import io.complicated.stereostream.utils.CommonErrorHandlerRedirector;
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
    //private View mRoomItemView;
    //private TextView getError()View;

    private void showEmptyRoomsView() {
        mContentViewSwitcher.setDisplayedChild(0);
    }

    private void showListOfRoomsView() {
        mContentViewSwitcher.setDisplayedChild(1);
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        mSharedPrefs.Init(getApplicationContext());
        mUtils.Init(savedInstanceState, getIntent(), mSharedPrefs);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.rooms_toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton add_btn = (FloatingActionButton) findViewById(R.id.rooms_add_btn);
        if (add_btn != null) add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                startActivity(new Intent(RoomsActivity.this, CreateRoomActivity.class));
            }
        });

        final String accessToken = mUtils.getFromLocalOrCache("access_token");

        if (accessToken == null) {
            Log.e("RoomsActivity", "access_token is null; redirecting...");
            startActivityForResult(new Intent(this, AuthActivity.class), CREATE);
            return;
        }

        mRoomsClient = new RoomsClient(this, accessToken);

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
        if (newRoom == null)
            loadRooms(null);
        else
            loadRooms(newRoom);
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
        if (mLoadRoomsTask != null)
            return;

        showProgress(true);
        mLoadRoomsTask = new LoadRoomsTask(this, newRoom);
        mLoadRoomsTask.execute((Void) null);
    }

    /**
     * Represents an asynchronous task
     */
    public static final class LoadRoomsTask extends AsyncTask<Void, Void,
            ErrorOrEntity<ListRooms>> {

        final Room mNewRoom;
        private final WeakReference<RoomsActivity> mWeakActivity;

        LoadRoomsTask(final RoomsActivity activity, final Room newRoom) {
            mNewRoom = newRoom;
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        protected final ErrorOrEntity<ListRooms> doInBackground(final Void... params) {
            return mRoomsClient.getSync();
        }

        @Override
        protected final void onPostExecute(final ErrorOrEntity<ListRooms> err_res) {
            final RoomsActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mLoadRoomsTask = null;
            activity.showProgress(false);

            if (err_res.success()) {
                final RoomAdapter roomAdapter = new RoomAdapter(
                        activity.getApplicationContext(), err_res.getEntity().getRooms());
                if (roomAdapter.getCount() > 0) {
                    activity.mContentListView.setAdapter(roomAdapter);
                    activity.showListOfRoomsView();
                } else {
                    activity.mInfoMsg.setText(activity.getString(R.string.empty_rooms));
                    activity.showEmptyRoomsView();
                }
            } else {
                activity.mCommonErrorHandlerRedirector.process(err_res);
                activity.showEmptyRoomsView();
                if (err_res.getErrorResponse() == null) {
                    err_res.getException().printStackTrace(System.err);
                    activity.mInfoMsg.setTextColor(Color.RED);
                    activity.mInfoMsg.setText(Formatters.ExceptionFormatter(err_res.getException()));
                } else activity.mInfoMsg.setText(err_res.getCode() == 404 ?
                        activity.getString(R.string.empty_rooms) :
                        err_res.getErrorResponse().toString());
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
