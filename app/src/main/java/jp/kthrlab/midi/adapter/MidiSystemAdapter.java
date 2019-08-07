package jp.kthrlab.midi.adapter;

import android.content.Context;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiManager;
import android.os.Handler;
import android.os.Looper;

import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;

public class MidiSystemAdapter {
    private Context context;
    private MidiManager aMidiManager;

    public MidiSystemAdapter(Context context) {
        this.context = context;
        aMidiManager = (MidiManager)context.getSystemService(Context.MIDI_SERVICE);
    }

    public MidiDeviceInfo[] getDevices() {
        return  aMidiManager.getDevices();
    }

    public void adaptAndroidMidiDeviceToKshoji() {
        System.out.println("MidiSystemAdapter adapt... " + aMidiManager.getDevices().length);

        for (MidiDeviceInfo info : aMidiManager.getDevices()) {
            addKMidiDevice(info);
        }

        aMidiManager.registerDeviceCallback(new MidiManager.DeviceCallback() {
            @Override
            public void onDeviceAdded( MidiDeviceInfo info ) {
                System.out.println("onDeviceAdded...");
                System.out.println(info.toString());
                addKMidiDevice(info);
            }

            @Override
            public void onDeviceRemoved( MidiDeviceInfo info ) {
                System.out.println("onDeviceRemoved...");
                System.out.println(info.toString());
                removeKMidiDevice(info);
            }

            @Override
            public void onDeviceStatusChanged(MidiDeviceStatus status) {
                System.out.println("onDeviceStatusChanged...");
                System.out.println(status.toString());
            }
        }, new Handler(Looper.getMainLooper()));

    }

    private void addKMidiDevice(MidiDeviceInfo aInfo) {
        KMidiDevice kMidiDevice = new KMidiDevice(aMidiManager, aInfo);
        try {
            // Condition 'MidiSystem.getMidiDevice(kMidiDevice.getDeviceInfo()) == null' is always 'false'
            MidiSystem.getMidiDevice(kMidiDevice.getDeviceInfo());
        } catch (Exception e) {
            e.printStackTrace();
            MidiSystem.addMidiDevice(kMidiDevice);

            // TODO: add if(isSynth)
//            if (aInfo.getInputPortCount() > 0) {
//                try {
//                    // Condition 'MidiSystem.getSynthesizer() == null' is always 'false'
//                    MidiSystem.getSynthesizer();
//                } catch (MidiUnavailableException e1) {
//                    e1.printStackTrace();
//                    MidiSystem.addSynthesizer(new KSynthesizer(kMidiDevice));
//                }
//            }
        }
    }

    private void removeKMidiDevice(MidiDeviceInfo info) {
        try {
            MidiSystem.removeMidiDevice(MidiSystem.getMidiDevice(KMidiDevice.getKInfo(info)));
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

}
