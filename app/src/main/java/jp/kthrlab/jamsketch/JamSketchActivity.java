package jp.kthrlab.jamsketch;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import jp.kshoji.javax.sound.midi.UsbMidiSystem;
import jp.kthrlab.midi.adapter.MidiSystemAdapter;
import processing.android.CompatUtils;
import processing.android.PFragment;
import processing.core.PApplet;

public class JamSketchActivity extends AppCompatActivity {
    private PApplet sketch;
    UsbMidiSystem ums;

    static Resources res = null;
    static Context context = null;

    public static Context getMyContext() {
        return context;
    }
    public static Resources getMyResources() {
        return res;
    }

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

        res = getResources();
        context = this;
        sketch = new JamSketch();
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

}
