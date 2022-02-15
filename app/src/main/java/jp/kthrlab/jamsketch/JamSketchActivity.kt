package jp.kthrlab.jamsketch

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import jp.kshoji.javax.sound.midi.UsbMidiSystem
import jp.kthrlab.midi.adapter.MidiSystemAdapter
import processing.android.CompatUtils
import processing.android.PFragment
import processing.core.PApplet

class JamSketchActivity : AppCompatActivity() {
    private var frame: FrameLayout? = null
    private var sketch: PApplet? = null
    var ums: UsbMidiSystem? = null
    var sharedPreferences: SharedPreferences? = null

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
    fun getSharedPreferencesBoolean(key: String?, defValue: Boolean): Boolean {
        return sharedPreferences!!.getBoolean(key, defValue)
    }

    fun putSharedPreferencesBoolean(key: String?, value: Boolean) {
        sharedPreferences!!.edit()
            .putBoolean(key, value)
            .apply()
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myResources = resources
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        frame = FrameLayout(this)
        frame!!.id = CompatUtils.getUniqueViewId()
        setContentView(
            frame, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        ums = UsbMidiSystem(this)
        ums!!.initialize()
        MidiSystemAdapter(this).adaptAndroidMidiDeviceToKshoji()
        if (getSharedPreferencesBoolean(
                JamSketchOnboardingFragment.SHOW_ONBOARDING_PREF_NAME,
                Config.SHOW_ONBOARDING
            )
        ) {
            startOnboarding()
        } else {
            startJamSketch()
        }
    }

    fun startOnboarding() {
        JamSketchOnboardingFragment().setView(frame!!, this)
    }

    fun startJamSketch() {
        sketch = JamSketch(this)
        val fragment = PFragment(sketch)
        fragment.setView(frame, this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (sketch != null) {
            sketch!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (sketch != null) {
            sketch!!.onNewIntent(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ums!!.terminate()
    }

    companion object {
        private const val TAG = "JamSketchActivity"
        var myResources: Resources? = null
            private set
    }
}