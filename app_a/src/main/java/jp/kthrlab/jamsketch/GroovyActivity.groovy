package jp.kthrlab.jamsketch

import android.content.Intent
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout
import jp.crestmuse.cmxtest.R
import processing.android.CompatUtils
import processing.core.PApplet

public class GroovyActivity  extends AppCompatActivity{
    private PApplet sketch

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        FrameLayout frame = new FrameLayout(this)
        frame.setId(CompatUtils.getUniqueViewId())
        setContentView(R.layout.activity_jam_sketch)

//        setContentView(frame, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT)) //R.layout.activity_jam_sketch);
//        sketch = new  MyPianoRoll() //JamSketch()
//        PFragment fragment = new PFragment(sketch)
//        fragment.setView(frame, this)
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (sketch != null) {
            sketch.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (sketch != null) {
            sketch.onNewIntent(intent)
        }
    }

}
