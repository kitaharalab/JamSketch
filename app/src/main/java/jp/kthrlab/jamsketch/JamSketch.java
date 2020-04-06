package jp.kthrlab.jamsketch;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.stream.IntStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jp.crestmuse.cmx.filewrappers.SCCDataSet;
import jp.crestmuse.cmx.misc.PianoRoll;
import jp.crestmuse.cmx.processing.DeviceNotAvailableException;
import jp.crestmuse.cmx.processing.gui.SimplePianoRoll;
import jp.crestmuse.cmx.sound.SoundUtils;
import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Sequencer;
import jp.kshoji.javax.sound.midi.impl.SequencerImpl;

public class JamSketch extends SimplePianoRoll {
    private static final String TAG = "JamSketch";

    private MelodyData2 melodyData;
    private boolean nowDrawing = false;
    private Control control;
    private boolean initiated = false;
    private CloudFunctionsHelper cloudFunctionsHelper;
    private JamSketchActivity jamSketchActivity;
    private Checkbox sendGranted;

    public JamSketch(JamSketchActivity jamSketchActivity) {
        this.jamSketchActivity = jamSketchActivity;
    }

    @Override
    public void settings() {
        size(1200, 700);
    }

    @Override
    public void setup() {
        super.setup();

        orientation(LANDSCAPE);

        control = new Control(this);
        control.addButton("startMusic")
                .setLabel("Start")
                .setPosition(10, 640)
                .setSize(110, 50);

        control.addButton("stopPlayMusic").
                setLabel("Stop").setPosition(130, 640).setSize(110, 50);
        control.addButton("resetMusic").
                setLabel("Reset").setPosition(250, 640).setSize(110, 50);
//        p5ctrl.addButton("loadCurve").
//                setLabel("Load").setPosition(300, 645).setSize(120, 40);
        control.addButton("showMidiOutChooser").
                setLabel("MidiOut").setPosition(370, 640).setSize(110, 50);

        sendGranted = control.addCheckbox("sendGranted");
        sendGranted.setLabel("Send melody to kthrlab.jp")
                .setLabelSize(130, 50)
                .setPosition(800, 655)
                .setSize(20, 20);

        control.addButton("sendGood").
                setLabel("Good↑").setPosition(960, 640).setSize(110, 50);
        control.addButton("sendBad").
                setLabel("Bad↓").setPosition(1080, 640).setSize(110, 50);

    }

    void icon(boolean value) {
        println("got an event for icon", value);
    }

    void checkMidiOutDeviceInfo() {
        try {
            if (SoundUtils.getMidiOutDeviceInfo().size() <= 0) {
                final String appPackageName = "net.volcanomobile.fluidsynthmidi"; // getPackageName() from Context or Activity object
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    void initData() {
        Log.d(TAG, "initData()");

        String filename = Config.MIDFILENAME;
//        println(melodyData.getFullChordProgression());
//        melodyData = new MelodyData(filename, width,  this, this, jamSketchActivity);
        melodyData = new MelodyData2(Config.MIDFILENAME, width, this, this, new Config());
            try {
                smfread(melodyData.getScc().getMIDISequence());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
//            } catch (InvalidMidiDataException e) {
//                e.printStackTrace();
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        SCCDataSet.Part part = melodyData.getScc().getFirstPartWithChannel(1);
            setDataModel(
                    part.getPianoRollDataModel(
                            Config.INITIAL_BLANK_MEASURES,
                            Config.INITIAL_BLANK_MEASURES + Config.NUM_OF_MEASURES
                    ));

        initiated = true;
    }

    @Override
    public void draw() {
        super.draw();
        strokeWeight(3);

        if (initiated) {
            drawCurve();
            if (getCurrentMeasure() == Config.NUM_OF_MEASURES - 1)
                processLastMeasure();
            enhanceCursor();
            drawProgress();
        }
    }

    void drawCurve() {
        stroke(0, 0, 255);
        IntStream.range(0, melodyData.getCurve1().size()-1).forEach(i ->{
            if (melodyData.getCurve1().get(i) != null && melodyData.getCurve1().get(i + 1) != null) {
                line(i, melodyData.getCurve1().get(i), i+1, melodyData.getCurve1().get(i + 1));
            }
        });
    }

    void storeCursorPosition() {
        if ((Config.ON_DRAG_ONLY || nowDrawing) && isInside(mouseX, mouseY) && getSequencer().getTickPosition() < getSequencer().getTickLength()) {
            int m1 = x2measure(mouseX);
            int m0 = x2measure(pmouseX);
//            System.out.println("m1:" + m1 + ", m0:" + m0 + ", mouseX:" + mouseX + ", pmouseX:" + pmouseX);
            if (0 <= m0) {
                if (pmouseX < mouseX) {
                    IntStream.rangeClosed(pmouseX, mouseX).forEach(i -> {
                        melodyData.getCurve1().set(i, mouseY);
                    });
                }
                melodyData.updateCurve(pmouseX, mouseX);
//                if (m1 > m0) {
//                    melodyData.updateCurve(m0 % Config.NUM_OF_MEASURES);
//                }
            }
        }
    }

    void processLastMeasure() {
        makeLog("melody");
        if (Config.MELODY_RESETING) {
            getDataModel().shiftMeasure(Config.NUM_OF_MEASURES);
            melodyData.resetCurve();
        }
        melodyData.getEngine().setFirstMeasure(getDataModel().getFirstMeasure());
    }

    void enhanceCursor() {
        if (Config.CURSOR_ENHANCED) {
            fill(255, 0, 0);
            ellipse(mouseX, mouseY, 10, 10);
        }
    }

    void drawProgress() {
        if (isNowPlaying()) {
            PianoRoll.DataModel dataModel = getDataModel();
            int m = getCurrentMeasure() + dataModel.getFirstMeasure() -
                    Config.INITIAL_BLANK_MEASURES + 1;
            int mtotal = dataModel.getMeasureNum() * Config.REPEAT_TIMES;
            textSize(32);
            fill(0, 0, 0);
            text(m + " / " + mtotal, 590, 665);
        }
    }

    public void startMusic() {
        if (!initiated) {
            try {
                initData();
            } catch (DeviceNotAvailableException e) {
                e.printStackTrace();
                showMidiOutChooser();
            }
        }

        if (!isNowPlaying()) {
            // add for debug 20190624 fujii
            Sequencer seqencer = getSequencer();
            System.out.println("startMusic() MicrosecondPosition:" + seqencer.getMicrosecondPosition() + " MicrosecondLength:" + seqencer.getMicrosecondLength() + " getFirstMeasure():" + getDataModel().getFirstMeasure());
            if (seqencer.getMicrosecondPosition() >= seqencer.getMicrosecondLength()) {
                seqencer.setLoopStartPoint(0);
                setTickPosition(0);
                getDataModel().setFirstMeasure(Config.INITIAL_BLANK_MEASURES);
                System.out.println("reset>>> MicrosecondPosition:" + seqencer.getMicrosecondPosition() + " MicrosecondLength:" + seqencer.getMicrosecondLength() + " getFirstMeasure():" + getDataModel().getFirstMeasure());
            }
            playMusic();
        }
    }

    public void stopPlayMusic() {
        if (isNowPlaying()) {
            stopMusic();

            // add 20190619 fujii
            ((SequencerImpl)getSequencer()).setLoopStartPoint(getTickPosition());
        }
    }

    public void resetMusic() {
        if (!isNowPlaying()) {

            initData();
            setTickPosition(0);
            getDataModel().setFirstMeasure(Config.INITIAL_BLANK_MEASURES);
            makeLog("reset");
        }
    }

    public void sendLike() {
        makeLog("good");
    }

    public void sendUm() {
        makeLog("bad");
    }

    private void makeLog(String action) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH-mm-ss_E", Locale.ENGLISH);
        String logname = sdf.format(Calendar.getInstance().getTime());

        try {
            if ("melody".equals(action)) {
                if (sendGranted.checked()) {
                    // midi
                    uploadFile(logname + "_melody.mid",
                            new ByteArrayInputStream(melodyData.getScc().toWrapper().toMIDIXML().getSMFByteArray()),
                            "audio/midi");

                    // scc
                    ByteArrayOutputStream outputStreamScc = new ByteArrayOutputStream();
                    TransformerFactory.newInstance().newTransformer().transform(new DOMSource(melodyData.getScc().toWrapper().getXMLDocument()), new StreamResult(outputStreamScc));
                    uploadFile(logname + "_melody.sccxml.zip",
                            new ByteArrayInputStream(outputStreamScc.toByteArray()),
                            "text/xml");

                    // json
                    uploadFile(logname + "_curve.json",
                            new ByteArrayInputStream(new Gson().toJson(melodyData.getCurve1()).getBytes()),
                            "application/json");

//                        // screenshot
//                        ByteArrayOutputStream outputStreamImg = new ByteArrayOutputStream();
//                        get();
//                        save("test");
                }

            } else {
                uploadFile(logname + "_" + action + ".txt",
                        new ByteArrayInputStream(action.getBytes()),
                        "text/plain");
            }

        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private void uploadFile(String fileName, InputStream inputStream, String mimeType) {
        if( cloudFunctionsHelper == null) cloudFunctionsHelper = new CloudFunctionsHelper();
        cloudFunctionsHelper.uploadFile(fileName, inputStream, mimeType)
                .addOnSuccessListener(fileId -> Log.d(TAG, fileId))
                .addOnFailureListener(exception ->
                        Log.e(TAG,"Couldn't create file.", exception));

    }

    public void showMidiOutChooser() {
        checkMidiOutDeviceInfo();
        showMidiOutChooser(jamSketchActivity.getSupportFragmentManager(), android.R.layout.simple_list_item_1);
    }

    public void loadCurve() {
    }

    @Override
    public void mousePressed() {
        nowDrawing = true;
    }

    @Override
    public void mouseReleased() {
        nowDrawing = false;
        if (isInside(mouseX, mouseY)) {
            println(x2measure(mouseX));
            println(Config.NUM_OF_MEASURES);
            if (!melodyData.getEngine().automaticUpdate()) {
                melodyData.getEngine().outlineUpdated(
                        x2measure(mouseX) % Config.NUM_OF_MEASURES,
                        Config.DIVISION - 1);
            }
        }
    }

    @Override
    public void mouseDragged() {
        if (initiated) {
            storeCursorPosition();
        }
    }


//    @Override
//    void keyReleased() {
//        if (key == ' ') {
//            if (isNowPlaying()) {
//                stopMusic();
//            } else {
//                setTickPosition(0);
//                getDataModel().setFirstMeasure(res.getInteger(R.integer.INITIAL_BLANK_MEASURES));
//                playMusic();
//            }
//        } else if (key == 'b') {
//            setNoteVisible(!isNoteVisible());
//            println("Visible=${isVisible()}");
//        } else if (key == 'u') {
//            melodyData.updateCurve("all");
//        }
//    }


}
