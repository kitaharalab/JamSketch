package jp.kthrlab.jamsketch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import jp.kshoji.javax.sound.midi.UsbMidiSystem;
import jp.kthrlab.midi.adapter.MidiSystemAdapter;
import processing.android.CompatUtils;
import processing.android.PFragment;
import processing.core.PApplet;

public class JamSketchActivity extends AppCompatActivity {
    private static final String TAG = "JamSketchActivity";
    private static Resources resources;
    private FrameLayout frame;

    private PApplet sketch;
    UsbMidiSystem ums;
    SharedPreferences sharedPreferences;

    public static Resources getMyResources() {
        return resources;
    }
//    public String getSharedPreferencesString(String key, String defValue) {
//        return sharedPreferences.getString(key, defValue);
//    }
//
//    public void putSharedPreferencesString(String key, String value) {
//        sharedPreferences.edit()
//                .putString(key, value)
//                .apply();
//    }
//
    public boolean getSharedPreferencesBoolean(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    public void putSharedPreferencesBoolean(String key, boolean value) {
        sharedPreferences.edit()
                .putBoolean(key, value)
                .apply();
    }
//
//    public int getSharedPreferencesInt(String key, int defValue) {
//        return sharedPreferences.getInt(key, defValue);
//    }
//
//    public void putSharedPreferencesInt(String key, int value) {
//        sharedPreferences.edit()
//                .putInt(key, value)
//                .apply();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resources = getResources();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        frame = new FrameLayout(this);
        frame.setId(CompatUtils.getUniqueViewId());
        setContentView(frame, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        ums = new UsbMidiSystem(this);
        ums.initialize();
        new MidiSystemAdapter(this).adaptAndroidMidiDeviceToKshoji();

        if (getSharedPreferencesBoolean(JamSketchOnboardingFragment.SHOW_ONBOARDING_PREF_NAME, Config.SHOW_ONBOARDING)) {
            startOnboarding();
        } else {
            startJamSketch();
        }
    }

    public void startOnboarding() {
        new JamSketchOnboardingFragment().setView(frame, this);
    }

    public void startJamSketch() {
        sketch = new JamSketch(this);
        PFragment fragment = new PFragment(sketch);
        fragment.setView(frame, this);
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



}
