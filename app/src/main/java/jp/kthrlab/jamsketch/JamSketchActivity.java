package jp.kthrlab.jamsketch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import jp.kshoji.javax.sound.midi.UsbMidiSystem;
import jp.kthrlab.midi.adapter.MidiSystemAdapter;
import processing.android.CompatUtils;
import processing.android.PFragment;
import processing.core.PApplet;

//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;

public class JamSketchActivity extends AppCompatActivity {
    private static final String TAG = "JamSketchActivity";
//    private static final int REQUEST_CODE_SIGN_IN = 1;
//    private DriveServiceHelper mDriveServiceHelper;

    private PApplet sketch;
    UsbMidiSystem ums;

//    static Resources res = null;
//    static Context context = null;
//
//    public static Context getMyContext() {
//        return context;
//    }
//    public static Resources getMyResources() {
//        return res;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        // Authenticate the user. For most apps, this should be done when the user performs an
//        // action that requires Drive access rather than in onCreate.
//        requestSignIn();

        FrameLayout frame = new FrameLayout(this);
        frame.setId(CompatUtils.getUniqueViewId());
//        setContentView(R.layout.activity_jam_sketch);
        setContentView(frame, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                        ViewGroup.LayoutParams.MATCH_PARENT));

        ums = new UsbMidiSystem(this);
        ums.initialize();

        new MidiSystemAdapter(this).adaptAndroidMidiDeviceToKshoji();

//        res = getResources();
//        context = this;

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

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
//        switch (requestCode) {
//            case REQUEST_CODE_SIGN_IN:
//                if (resultCode == Activity.RESULT_OK && resultData != null) {
//                    handleSignInResult(resultData);
//                }
//                break;
//        }
//
//        super.onActivityResult(requestCode, resultCode, resultData);
//
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ums.terminate();
    }

//    /**
//     * Handles the {@code result} of a completed sign-in activity initiated from {@link
//     * #requestSignIn()}.
//     */
//    private void handleSignInResult(Intent result) {
//        GoogleSignIn.getSignedInAccountFromIntent(result)
//                .addOnSuccessListener(googleAccount -> {
//                    Log.d(TAG, "Signed in as " + googleAccount.getEmail());
//
//                    // Use the authenticated account to sign in to the Drive service.
//                    GoogleAccountCredential credential =
//                            GoogleAccountCredential.usingOAuth2(
//                                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
//                    credential.setSelectedAccount(googleAccount.getAccount());
//                    Drive googleDriveService =
//                            new Drive.Builder(
//                                    AndroidHttp.newCompatibleTransport(),
//                                    new GsonFactory(),
//                                    credential)
//                                    .setApplicationName("JamSketch")
//                                    .build();
//
//                    // The DriveServiceHelper encapsulates all REST API and SAF functionality.
//                    // Its instantiation is required before handling any onClick actions.
//                    mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
//                })
//                .addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
//    }
//
//    public DriveServiceHelper getDriveServiceHelper() {return mDriveServiceHelper;}
//
//    /**
//     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN}.
//     */
//    private void requestSignIn() {
//        Log.d(TAG, "Requesting sign-in");
//
//        GoogleSignInOptions signInOptions =
//                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestEmail()
//                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
//                        .build();
//        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);
//
//        // The result of the sign-in Intent is handled in onActivityResult.
//        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
//    }

}
