package jp.kthrlab.midi.adapter;

import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
//import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.Transmitter;

public class KMidiDevice implements jp.kshoji.javax.sound.midi.MidiDevice, Handler.Callback {

    private MidiManager aMidiManager;
    private MidiDeviceInfo aMidiDeviceInfo;
    private MidiDevice aMidiDevice;
    private Info kInfo;
    private boolean isOpen;
    private List<Receiver> kReceivers;
    private List<Transmitter> kTransmitters;

    public static Info getKInfo(MidiDeviceInfo aMidiDeviceInfo) {
        Bundle bundle = aMidiDeviceInfo.getProperties();
        return new Info(bundle.getString(MidiDeviceInfo.PROPERTY_NAME) == null ? "" : bundle.getString(MidiDeviceInfo.PROPERTY_NAME),
                bundle.getString(MidiDeviceInfo.PROPERTY_MANUFACTURER) == null ? "" : bundle.getString(MidiDeviceInfo.PROPERTY_MANUFACTURER),
                bundle.getString(MidiDeviceInfo.PROPERTY_SERIAL_NUMBER) == null ? "" : bundle.getString(MidiDeviceInfo.PROPERTY_SERIAL_NUMBER),
                bundle.getString(MidiDeviceInfo.PROPERTY_PRODUCT) == null ? "" : bundle.getString(MidiDeviceInfo.PROPERTY_PRODUCT));
    }

    public KMidiDevice(MidiManager aMidiManager, MidiDeviceInfo aMidiDeviceInfo) {
        this.aMidiManager = aMidiManager;
        this.aMidiDeviceInfo = aMidiDeviceInfo;
        init();
    }

    private void init() {
        kInfo = getKInfo(aMidiDeviceInfo);
        kReceivers = new ArrayList<Receiver>();
        kTransmitters = new ArrayList<Transmitter>();

        for (final MidiDeviceInfo.PortInfo portInfo : aMidiDeviceInfo.getPorts()) {
            System.out.println("portInfo.getType():" + portInfo.getType());
            if (portInfo.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT) {
                kReceivers.add(new KReceiver(this, portInfo));
            } else if (portInfo.getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT) {
                kTransmitters.add(new KTransmitter(this, portInfo));
            }
        }
    }

    public MidiDevice getAMidiDevice(boolean shouldOpen) {
//        System.out.println("getAMidiDevice(" + shouldOpen + ") " + aMidiDeviceInfo);
//        System.out.println("Thread.currentThread().dumpStack():");
//        Thread.currentThread().dumpStack();

        if (!isOpen() && shouldOpen) {
            aMidiManager.openDevice(aMidiDeviceInfo,
                    new MidiManager.OnDeviceOpenedListener() {
                        @Override
                        public void onDeviceOpened(MidiDevice device) {
                            if (device == null) {
                                System.out.println("device == null");
                            } else {
                                System.out.println("device opened: " + device.getInfo().toString());
                                aMidiDevice = device;
                                isOpen = true;
                            }
                        }
                    },
                    null);

            // TODO: how to wait deviceOpend?
            while (!isOpen()) {
                System.out.println("waiting...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return aMidiDevice;
    }

    @NonNull
    @Override
    public Info getDeviceInfo() {
        return kInfo;
    }

    @Override
    public void open() throws MidiUnavailableException {
        getAMidiDevice(true);
    }

    @Override
    public void close() {
        if (aMidiDevice != null) {
            try {
                aMidiDevice.close();
                isOpen = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public long getMicrosecondPosition() {
        // time-stamping is not supported
        return -1;
    }

    @Override
    public int getMaxReceivers() {
        return kReceivers.size();
    }

    @Override
    public int getMaxTransmitters() {
        return kTransmitters.size();
    }

    @NonNull
    @Override
    public Receiver getReceiver() throws MidiUnavailableException {
        return kReceivers.get(0);
    }

    @NonNull
    @Override
    public List<Receiver> getReceivers() {
        return kReceivers;
    }

    @NonNull
    @Override
    public Transmitter getTransmitter() throws MidiUnavailableException {
        return kTransmitters.get(0);
    }

    @NonNull
    @Override
    public List<Transmitter> getTransmitters() {
        return kTransmitters;
    }

    @Override
    public boolean handleMessage (Message msg) {

        return false;
    }

}
