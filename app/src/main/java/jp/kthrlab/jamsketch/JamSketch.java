package jp.kthrlab.jamsketch;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.stream.IntStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
    public float scale = 1.0F;
    private int margin = 10;
    private int buttonWidth;
    private int buttonHeight;
    private float buttonY;
    private int currentMeasure;
    private int fullMeasure;
    //    private Checkbox sendGranted;

    public JamSketch(JamSketchActivity jamSketchActivity) {
        this.jamSketchActivity = jamSketchActivity;
    }

    @Override
    public void settings() {
//        pixelDensity = (int)displayDensity;
        scale = displayDensity;

        if (displayWidth >= displayHeight) {
            if ((int)(600*scale) > displayWidth) {
                scale = displayWidth/600.0F;
            }
            if ((int)(350*scale) > displayHeight) {
                scale = displayHeight/350.0F;
            }
        } else {
            if ((int)(600*scale) > displayHeight) {
                scale = displayHeight/600.0F;
            }
            if ((int)(350*scale) > displayWidth) {
                scale = displayWidth/350.0F;
            }
        }
        size((int)(600*scale), (int)(350*scale));
        setOctaveWidth(height*0.3);
        setKeyboardWidth(width/(Config.NUM_OF_MEASURES+1));
    }

    @Override
    public void setup() {
        super.setup();
        orientation(LANDSCAPE);

        buttonWidth = (width- margin*9)/8;
        buttonHeight = height/10 - margin*2;
        buttonY = height*9/10 + margin;

        control = new Control(this);
        control.addButton("startMusic")
                .setLabel("Start")
                .setPosition(margin, buttonY)
                .setSize(buttonWidth, buttonHeight);

        control.addButton("stopPlayMusic").
                setLabel("Stop").setPosition(margin*2 + buttonWidth, buttonY).setSize(buttonWidth, buttonHeight);
        control.addButton("resetMusic").
                setLabel("Reset").setPosition(margin*3 + buttonWidth*2, buttonY).setSize(buttonWidth, buttonHeight);
//        p5ctrl.addButton("loadCurve").
//                setLabel("Load").setPosition(300, 645).setSize(120, 40);
        control.addButton("showMidiOutChooser").
                setLabel("MidiOut").setPosition(margin*4 + buttonWidth*3, buttonY).setSize(buttonWidth, buttonHeight);

//        sendGranted = control.addCheckbox("sendGranted");
//        sendGranted.setLabel("Send melody to kthrlab.jp")
//                .setLabelSize(130, 50)
//                .setPosition(800, 655)
//                .setSize(20, 20);

        control.addButton("sendGood").
                setLabel("Good↑").setPosition(margin*7 + buttonWidth*6, buttonY).setSize(buttonWidth, buttonHeight);
        control.addButton("sendBad").
                setLabel("Bad↓").setPosition(margin*8 + buttonWidth*7, buttonY).setSize(buttonWidth, buttonHeight);
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
            drawProgress();
            if (getCurrentMeasure() == Config.NUM_OF_MEASURES - Config.NUM_OF_RESET_AHEAD)
                processLastMeasure();
            enhanceCursor();
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
        if ((Config.ON_DRAG_ONLY || nowDrawing) &&
                isInside(mouseX, mouseY) &&
                getSequencer().getTickPosition() < getSequencer().getTickLength()) {
            int m1 = x2measure(mouseX);
            int m0 = x2measure(pmouseX);
            if (0 <= m0) {
                if (pmouseX < mouseX) {
                    IntStream.rangeClosed(pmouseX, mouseX).forEach(i -> {
                        melodyData.getCurve1().set(i, mouseY);
                    });
                    melodyData.updateCurve(pmouseX, mouseX);
                }
//                if (m1 > m0) {
//                    melodyData.updateCurve(m0 % Config.NUM_OF_MEASURES);
//                }
            }
        }
    }

    void processLastMeasure() {
        makeLog("melody");
        if (Config.MELODY_RESETING) {
            if (currentMeasure < fullMeasure) {
                getDataModel().shiftMeasure(Config.NUM_OF_MEASURES);
            }
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
            currentMeasure = getCurrentMeasure() + dataModel.getFirstMeasure() -
                    Config.INITIAL_BLANK_MEASURES + 1;
            fullMeasure = dataModel.getMeasureNum() * Config.REPEAT_TIMES;
            textSize(16*scale);
            fill(0, 0, 0);
            text(currentMeasure + " / " + fullMeasure, (margin*6 + buttonWidth*5), buttonY + buttonHeight/2);
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
            resetData();
            resetSequencer();
            println("reset");
//            makeLog("reset");
        }
    }

    private void resetData() {
        getDataModel().setFirstMeasure(Config.INITIAL_BLANK_MEASURES);
        melodyData.getEngine().setFirstMeasure(getDataModel().getFirstMeasure());
        System.out.println("resetData>>> getCurrentMeasure() = " + getCurrentMeasure() + ", getCurrentBeat() = " + getCurrentBeat());
    }

    private void resetSequencer() {
        setTickPosition(0);
        Sequencer seqencer = getSequencer();
        seqencer.setLoopStartPoint(0);
        ((SequencerImpl)seqencer).refreshPlayingTrack();
        System.out.println("resetSequencer>>> MicrosecondPosition:" + seqencer.getMicrosecondPosition() + " MicrosecondLength:" + seqencer.getMicrosecondLength() + " getFirstMeasure():" + getDataModel().getFirstMeasure());
    }

    @Override
    protected void musicStopped() {
        super.musicStopped();
        System.out.println("musicStopped>>> StoppedgetMicrosecondPosition() = " + getMicrosecondPosition());
        System.out.println("musicStopped>>> getSequencer().getMicrosecondLength() = " + getSequencer().getMicrosecondLength());
        if (getMicrosecondPosition() >= getSequencer().getMicrosecondLength()) {
            resetMusic();
        }
    }

    public void sendGood() {
        makeLog("good");
    }

    public void sendBad() {
        makeLog("bad");
    }

    private void makeLog(String action) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH-mm-ss_E", Locale.ENGLISH);
        String logname = sdf.format(Calendar.getInstance().getTime());

        if ("melody".equals(action)) {

            // Send logs
//                if (sendGranted.checked()) {
                // midi
                uploadFile(logname + "_melody.mid",
                        melodyData,
//                            new ByteArrayInputStream(melodyData.getScc().toWrapper().toMIDIXML().getSMFByteArray()),
                        "audio/midi");

                // sccxml
//                    ByteArrayOutputStream outputStreamScc = new ByteArrayOutputStream();
//                    TransformerFactory.newInstance().newTransformer().transform(new DOMSource(melodyData.getScc().toWrapper().getXMLDocument()), new StreamResult(outputStreamScc));
                uploadFile(logname + "_melody.sccxml",
                        melodyData,
//                            new ByteArrayInputStream(outputStreamScc.toByteArray()),
                        "text/xml");

                // json
                uploadFile(logname + "_curve.json",
                        melodyData,
//                            new ByteArrayInputStream(new Gson().toJson(melodyData.getCurve1()).getBytes()),
                        "application/json");

//                        // screenshot
//                        ByteArrayOutputStream outputStreamImg = new ByteArrayOutputStream();
//                        get();
//                        save("test");
//                }

        } else {
            uploadFile(logname + "_" + action + ".txt",
                    action,
                    "text/plain");
        }

    }

    private void uploadFile(String fileName, MelodyData2 melodyData, String mimeType) {
        if( cloudFunctionsHelper == null) cloudFunctionsHelper = new CloudFunctionsHelper();
        if ("audio/midi".equals(mimeType)) {
            cloudFunctionsHelper.uploadScc(fileName, melodyData, mimeType)
                    .addOnSuccessListener(fileId -> Log.d(TAG, fileId))
                    .addOnFailureListener(exception ->
                            Log.e(TAG,"Couldn't create file.", exception));
        } else if ("text/xml".equals(mimeType)) {
            cloudFunctionsHelper.uploadSccxml(fileName, melodyData, mimeType)
                    .addOnSuccessListener(fileId -> Log.d(TAG, fileId))
                    .addOnFailureListener(exception ->
                            Log.e(TAG,"Couldn't create file.", exception));

        } else if ("application/json".equals(mimeType)) {
            cloudFunctionsHelper.uploadJson(fileName, melodyData, mimeType)
                    .addOnSuccessListener(fileId -> Log.d(TAG, fileId))
                    .addOnFailureListener(exception ->
                            Log.e(TAG,"Couldn't create file.", exception));
        }
    }

    private void uploadFile(String fileName, String text, String mimeType) {
        if( cloudFunctionsHelper == null) cloudFunctionsHelper = new CloudFunctionsHelper();
        cloudFunctionsHelper.uploadText(fileName, text, mimeType)
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
//            println(x2measure(mouseX));
//            println(Config.NUM_OF_MEASURES);
            if (melodyData != null && !melodyData.getEngine().automaticUpdate()) {
                melodyData.getEngine().outlineUpdated(
                        x2measure(mouseX) % Config.NUM_OF_MEASURES,
                        Config.DIVISION - 1);
            }
        }
    }

    @Override
    public void mouseDragged() {
        if (initiated && isNowPlaying()) {
//            println("mouseX = " + mouseX + ", beat2x = " + beat2x(Config.NUM_OF_MEASURES, getCurrentBeat()));
            if(mouseX > beat2x(getCurrentMeasure(), getCurrentBeat()) + 10) {
                storeCursorPosition();
            }
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
