package jp.kthrlab.jamsketch;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import jp.kshoji.driver.midi.device.MidiInputDevice;
import jp.kshoji.driver.midi.device.MidiOutputDevice;
import jp.kshoji.javax.sound.midi.UsbMidiSystem;
import processing.android.CompatUtils;
import processing.android.PFragment;
import processing.core.PApplet;

public class JamSketchActivity extends AbstractMultipleMidiAppCompatActivity {
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
        setContentView(frame, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                        ViewGroup.LayoutParams.MATCH_PARENT)); //R.layout.activity_jam_sketch);

        ums = new UsbMidiSystem(this);
        ums.initialize();



        res = getResources();
        context = this;
        sketch = new JamSketch();
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

    @Override
    public void onDeviceAttached(@NonNull UsbDevice usbDevice) {
    }

    @Override
    public void onMidiInputDeviceAttached(@NonNull MidiInputDevice midiInputDevice) {

    }

    @Override
    public void onMidiInputDeviceDetached(@NonNull MidiInputDevice midiInputDevice) {

    }

    @Override
    public void onMidiOutputDeviceAttached(@NonNull MidiOutputDevice midiOutputDevice) {
        System.out.println("onMidiOutputDeviceAttached" + midiOutputDevice.getProductName());
///        log( "USB MIDI Device " + midiOutputDevice.getUsbDevice().getDeviceName() + ": attached");
        midiOutputDevice.resume();
    }

    @Override
    public void onDeviceDetached(@NonNull UsbDevice usbDevice) {

    }

    @Override
    public void onMidiOutputDeviceDetached(@NonNull MidiOutputDevice midiOutputDevice) {
        System.out.println("onMidiOutputDeviceDetached" + midiOutputDevice.getProductName());
//        log( "USB MIDI Device " + midiOutputDevice.getUsbDevice().getDeviceName() + ": detached");
        midiOutputDevice.suspend();
    }

    @Override
    public void onMidiMiscellaneousFunctionCodes(@NonNull MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {

    }

    @Override
    public void onMidiCableEvents(@NonNull MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {

    }

    @Override
    public void onMidiSystemCommonMessage(@NonNull MidiInputDevice sender, int cable, byte[] bytes) {

    }

    @Override
    public void onMidiSystemExclusive(@NonNull MidiInputDevice sender, int cable, byte[] systemExclusive) {

    }

    @Override
    public void onMidiNoteOff(@NonNull MidiInputDevice sender, int cable, int channel, int note, int velocity) {

    }

    @Override
    public void onMidiNoteOn(@NonNull MidiInputDevice sender, int cable, int channel, int note, int velocity) {

    }

    @Override
    public void onMidiPolyphonicAftertouch(@NonNull MidiInputDevice sender, int cable, int channel, int note, int pressure) {

    }

    @Override
    public void onMidiControlChange(@NonNull MidiInputDevice sender, int cable, int channel, int function, int value) {

    }

    @Override
    public void onMidiProgramChange(@NonNull MidiInputDevice sender, int cable, int channel, int program) {

    }

    @Override
    public void onMidiChannelAftertouch(@NonNull MidiInputDevice sender, int cable, int channel, int pressure) {

    }

    @Override
    public void onMidiPitchWheel(@NonNull MidiInputDevice sender, int cable, int channel, int amount) {

    }

    @Override
    public void onMidiSingleByte(@NonNull MidiInputDevice sender, int cable, int byte1) {

    }

    @Override
    public void onMidiTimeCodeQuarterFrame(@NonNull MidiInputDevice sender, int cable, int timing) {

    }

    @Override
    public void onMidiSongSelect(@NonNull MidiInputDevice sender, int cable, int song) {

    }

    @Override
    public void onMidiSongPositionPointer(@NonNull MidiInputDevice sender, int cable, int position) {

    }

    @Override
    public void onMidiTuneRequest(@NonNull MidiInputDevice sender, int cable) {

    }

    @Override
    public void onMidiTimingClock(@NonNull MidiInputDevice sender, int cable) {

    }

    @Override
    public void onMidiStart(@NonNull MidiInputDevice sender, int cable) {

    }

    @Override
    public void onMidiContinue(@NonNull MidiInputDevice sender, int cable) {

    }

    @Override
    public void onMidiStop(@NonNull MidiInputDevice sender, int cable) {

    }

    @Override
    public void onMidiActiveSensing(@NonNull MidiInputDevice sender, int cable) {

    }

    @Override
    public void onMidiReset(@NonNull MidiInputDevice sender, int cable) {

    }
}
