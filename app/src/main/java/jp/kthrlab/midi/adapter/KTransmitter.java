package jp.kthrlab.midi.adapter;

import android.media.midi.MidiReceiver;
import android.media.midi.MidiSender;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import jp.kshoji.javax.sound.midi.MidiDevice;
import jp.kshoji.javax.sound.midi.MidiDeviceTransmitter;
import jp.kshoji.javax.sound.midi.Receiver;

public class KTransmitter extends MidiSender implements MidiDeviceTransmitter {
    private Receiver kReceiver;
    private KMidiDevice kMidiDevice;
    private android.media.midi.MidiDevice aMidiDevice;
    private android.media.midi.MidiDeviceInfo.PortInfo aPortInfo;
    private android.media.midi.MidiOutputPort aMidiOutputPort;

    public KTransmitter(KMidiDevice kMidiDevice, android.media.midi.MidiDeviceInfo.PortInfo aPortInfo) {
        this.kMidiDevice = kMidiDevice;
        this.aPortInfo = aPortInfo;
    }

    @NonNull
    @Override
    public MidiDevice getMidiDevice() {
        return kMidiDevice;
    }

    @Override
    public void setReceiver(@Nullable Receiver receiver) {
        this.kReceiver = receiver;
    }

    @Nullable
    @Override
    public Receiver getReceiver() {
        return kReceiver;
    }

    @Override
    public void close() {
        kMidiDevice.close();
    }

    @Override
    public void onConnect(MidiReceiver receiver) {

        aMidiDevice = kMidiDevice.getAMidiDevice(false);

        if (aMidiDevice != null) {
            if (aMidiOutputPort == null) {
                aMidiOutputPort = aMidiDevice.openOutputPort(aPortInfo.getPortNumber());
            }
            aMidiOutputPort.connect(receiver);
        }
    }

    @Override
    public void onDisconnect(MidiReceiver receiver) {
        if (aMidiOutputPort != null) aMidiOutputPort.disconnect(receiver);
    }
}
