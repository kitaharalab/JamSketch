package jp.kthrlab.jamsketch

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.AssetManager
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

class JamSketchActivity : AppCompatActivity() {
    private var sketch: JamSketch//PApplet
    private lateinit var frameLayout: FrameLayout
    private lateinit var ums: UsbMidiSystem
    private lateinit var sharedPreferences: SharedPreferences

    init {
        sketch = JamSketch(this)
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
    fun getSharedPreferencesBoolean(key: String?, defValue: Boolean): Boolean {
        return sharedPreferences .getBoolean(key, defValue)
    }

    fun putSharedPreferencesBoolean(key: String?, value: Boolean) {
        sharedPreferences.edit()
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
        jamSketchResources = resources
        jamSketchAssets = assets
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        frameLayout = FrameLayout(this)
        frameLayout.id = CompatUtils.getUniqueViewId()
        setContentView(
            frameLayout,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        ums = UsbMidiSystem(this)
        ums.initialize()
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
        supportFragmentManager.beginTransaction()
            .add(frameLayout.id, JamSketchOnboardingFragment())
            .commit()
    }

    fun startJamSketch() {
        val fragment = PFragment(sketch)
        fragment.setView(frameLayout, this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        sketch.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        sketch.onNewIntent(intent)
    }

    override fun onStop() {
        super.onStop()
        sketch.exit()
        ums.terminate()
        if (BuildConfig.DEBUG) println("onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (BuildConfig.DEBUG) println("onDestroy()")
    }

    companion object {
        private const val TAG = "JamSketchActivity"
        lateinit var jamSketchResources: Resources
        lateinit var jamSketchAssets: AssetManager
    }

}