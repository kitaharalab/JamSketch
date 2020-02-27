package jp.kthrlab.jamsketch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.io.IOException;
import java.util.Collections;

import jp.kshoji.javax.sound.midi.UsbMidiSystem;
import jp.kthrlab.midi.adapter.MidiSystemAdapter;
import processing.android.CompatUtils;
import processing.android.PFragment;
import processing.core.PApplet;

public class JamSketchActivity extends AppCompatActivity {
    private static final String TAG = "JamSketchActivity";

    private DriveServiceHelper mDriveServiceHelper;

    private PApplet sketch;
    UsbMidiSystem ums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout frame = new FrameLayout(this);
        frame.setId(CompatUtils.getUniqueViewId());
//        setContentView(R.layout.activity_jam_sketch);
        setContentView(frame, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                        ViewGroup.LayoutParams.MATCH_PARENT));

        ums = new UsbMidiSystem(this);
        ums.initialize();

        new MidiSystemAdapter(this).adaptAndroidMidiDeviceToKshoji();

        sketch = new JamSketch(this);
        PFragment fragment = new PFragment(sketch);
        fragment.setView(frame, this);

        // midiouts[]
//        ((JamSketch)sketch).showMidiOutChooser();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (sketch != null) {
            sketch.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (sketch != null) {
            sketch.onNewIntent(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ums.terminate();
    }

    public DriveServiceHelper getDriveServiceHelper() {return mDriveServiceHelper;}

    private GoogleCredentials getServiceAccountCredential() {
//        GoogleCredential credential = null;
        GoogleCredentials credentials = null;
        try {
//            credential = GoogleCredential.fromStream(getResources().getAssets().open("credential/                    .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));
            credentials = ServiceAccountCredentials.fromStream(getResources().openRawResource(R.raw.credential))
                    .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return credentials;
    }

    public void createDriveServiceHelper() {
//        // Authenticate the user. For most apps, this should be done when the user performs an
//        // action that requires Drive access rather than in onCreate.
        createDriveServiceHelper(getServiceAccountCredential());
    }

    private void createDriveServiceHelper(Credentials credentials) {

                            Drive googleDriveService =
                            new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    new HttpCredentialsAdapter(credentials))
                                    .setApplicationName("JamSketch")
                                    .build();

        // The DriveServiceHelper encapsulates all REST API and SAF functionality.
        // Its instantiation is required before handling any onClick actions.
        mDriveServiceHelper = new DriveServiceHelper(googleDriveService);

    }

}
