package jp.kthrlab.midi.adapter;

import android.media.midi.MidiReceiver;

import androidx.annotation.NonNull;

import java.io.IOException;

import jp.kshoji.javax.sound.midi.MidiMessage;
import jp.kshoji.javax.sound.midi.Receiver;

public class KReceiver extends MidiReceiver implements Receiver {
    private KMidiDevice kMidiDevice;
    private android.media.midi.MidiDevice aMidiDevice;
    private android.media.midi.MidiDeviceInfo.PortInfo aPortInfo;
    private android.media.midi.MidiInputPort aMidiInputPort;

    public KReceiver(KMidiDevice kMidiDevice, android.media.midi.MidiDeviceInfo.PortInfo aPortInfo) {
        this.kMidiDevice = kMidiDevice;
        this.aPortInfo = aPortInfo;
    }

    @Override
    public void send(@NonNull MidiMessage message, long timeStamp) {

        aMidiDevice = kMidiDevice.getAMidiDevice(false);

        if (aMidiDevice != null) {
            if (aMidiInputPort == null) {
                aMidiInputPort = aMidiDevice.openInputPort(aPortInfo.getPortNumber());
            }

            try {
                aMidiInputPort.send(message.getMessage(), 0, message.getLength(), timeStamp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        try {
            if (aMidiInputPort != null) aMidiInputPort.close();
            if (aMidiDevice != null) aMidiDevice.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
    }
}
