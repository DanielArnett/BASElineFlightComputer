package com.platypii.baseline;

import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.cloud.UploadManager;
import com.platypii.baseline.events.AuthEvent;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.tracks.TrackFiles;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;

public class TrackActivity extends BaseActivity implements DialogInterface.OnClickListener {
    private static final String TAG = "TrackActivity";

    static final String EXTRA_TRACK_FILE = "TRACK_FILE";

    private Button syncButton;
    private AlertDialog alertDialog;

    private TrackFile trackFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jump);

        syncButton = findViewById(R.id.syncButton);

        // Load jump from extras
        final Bundle extras = getIntent().getExtras();
        if(extras != null) {
            final String extraTrackFile = extras.getString(EXTRA_TRACK_FILE);
            if(extraTrackFile != null) {
                final File trackDir = TrackFiles.getTrackDirectory(getApplicationContext());
                trackFile = new TrackFile(new File(trackDir, extraTrackFile));
            }
        }

        // Initial view updates
        updateViews();
        // Note: don't update auth views until we get a SyncEvent, since it would blink the sign in button
    }

    /**
     * Update view states (except for auth state)
     */
    private void updateViews() {
        if(trackFile != null) {
            final int uploadState = Services.cloud.uploads.getState(trackFile);
            if(uploadState == UploadManager.UPLOADED) {
                // Track uploaded, open TrackDataActivity
                final CloudData cloudData = Services.cloud.uploads.getCompleted(trackFile);
                Intents.openTrackDataActivity(this, cloudData);
                finish();
                return;
            }

            // Find views
            final TextView filenameLabel = findViewById(R.id.filename);
            final TextView filesizeLabel = findViewById(R.id.filesize);
            final TextView alertLabel = findViewById(R.id.alert_message);

            filenameLabel.setText(trackFile.getName());
            filesizeLabel.setText(trackFile.getSize());

            // Update view based on sign-in state
            if(isSignedIn() && uploadState != UploadManager.UPLOADING) {
                syncButton.setEnabled(true);
            } else {
                syncButton.setEnabled(false);
            }
            if(uploadState == UploadManager.UPLOADING) {
                alertLabel.setText(R.string.uploading);
                alertLabel.setVisibility(View.VISIBLE);
            } else {
                alertLabel.setVisibility(View.GONE);
            }
        }
    }

    public void clickSync(View v) {
        // Start upload
        firebaseAnalytics.logEvent("click_track_sync", null);
        Toast.makeText(getApplicationContext(), "Syncing track...", Toast.LENGTH_SHORT).show();
        Services.cloud.uploads.userUpload(trackFile);
        updateViews();
    }

    public void clickDelete(View v) {
        firebaseAnalytics.logEvent("click_track_delete_local_1", null);
        alertDialog = new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Delete this track?")
            .setMessage(R.string.delete_local)
            .setPositiveButton("Delete", this)
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * User clicked "ok" on delete track
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(which == DialogInterface.BUTTON_POSITIVE) {
            // Delete track
            firebaseAnalytics.logEvent("click_track_delete_local_2", null);
            deleteLocal();
        }
    }

    private void deleteLocal() {
        if(trackFile.delete()) {
            // Notify user
            Toast.makeText(getApplicationContext(), "Deleted " + trackFile.getName(), Toast.LENGTH_LONG).show();
            // Exit activity
            finish();
        } else {
            // Delete failed
            Toast.makeText(getApplicationContext(), "Failed to delete track " + trackFile.getName(), Toast.LENGTH_LONG).show();
        }
    }

    public void clickExport(View v) {
        firebaseAnalytics.logEvent("click_track_export", null);
        Intents.exportTrackFile(this, trackFile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Listen for sync and auth updates
        EventBus.getDefault().register(this);
        updateViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Dismiss alert to prevent context leak
        if(alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUploadSuccess(@NonNull SyncEvent.UploadSuccess event) {
        if(event.trackFile.getName().equals(trackFile.getName())) {
            // Track uploaded, open TrackActivity
            Toast.makeText(getApplicationContext(), "Track sync success", Toast.LENGTH_SHORT).show();
            Intents.openTrackDataActivity(this, event.cloudData);
            finish();
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUploadFailure(@NonNull SyncEvent.UploadFailure event) {
        if(event.trackFile.getName().equals(trackFile.getName())) {
            Log.e(TAG, "Failed to upload track: " + event.error);
            Toast.makeText(getApplicationContext(), "Track sync failed", Toast.LENGTH_LONG).show();
            updateViews();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthEvent(AuthEvent event) {
        // Signing in enables the sync button, so update button views:
        updateViews();
    }

}
