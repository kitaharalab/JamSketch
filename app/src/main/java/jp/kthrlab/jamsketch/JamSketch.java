package jp.kthrlab.jamsketch;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jp.crestmuse.cmx.filewrappers.SCCDataSet;
import jp.crestmuse.cmx.misc.PianoRoll;
import jp.crestmuse.cmx.processing.DeviceNotAvailableException;
import jp.crestmuse.cmx.processing.gui.SimplePianoRoll;
import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.Sequencer;
import jp.kshoji.javax.sound.midi.impl.SequencerImpl;

public class JamSketch extends SimplePianoRoll {
    private static final String TAG = "JamSketch";

    private MelodyData melodyData;
    private boolean nowDrawing = false;
    private Control control;
    private boolean inited = false;
    private List<List<Integer>> cachedCurve1 = new ArrayList<>();
    private DriveServiceHelper mDriveServiceHelper;
    private JamSketchActivity jamSketchActivity;

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
                .setPosition(20, 645)
                .setSize(120, 40);

        control.addButton("stopPlayMusic").
                setLabel("Stop").setPosition(160, 645).setSize(120, 40);
        control.addButton("resetMusic").
                setLabel("Reset").setPosition(300, 645).setSize(120, 40);
//        p5ctrl.addButton("loadCurve").
//                setLabel("Load").setPosition(300, 645).setSize(120, 40);
        control.addButton("showMidiOutChooser").
                setLabel("MidiOut").setPosition(440, 645).setSize(120, 40);

    }

    void icon(boolean value) {
        println("got an event for icon", value);
    }

    void initData() {

        // TODO: add condition 'if (midiouts[0] != null)'

        System.out.println("initData()");
        String filename = Config.MIDFILENAME;
//        println(melodyData.getFullChordProgression());
        melodyData = new MelodyData(filename, width,  this, this, jamSketchActivity);

            try {
                smfread(melodyData.scc.getMIDISequence());
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
        SCCDataSet.Part part = melodyData.scc.getFirstPartWithChannel(1);
            setDataModel(
                    part.getPianoRollDataModel(
                            Config.INITIAL_BLANK_MEASURES,
                            Config.INITIAL_BLANK_MEASURES + Config.NUM_OF_MEASURES
                    ));

        inited = true;
    }

    @Override
    public void draw() {
        super.draw();
        strokeWeight(3);

        if (inited) {
            drawCurve();

            if (getCurrentMeasure() == Config.NUM_OF_MEASURES - 1) processLastMeasure();

            enhanceCursor();
            drawProgress();
        }
    }

    void drawCurve() {
        stroke(0, 0, 255);
        IntStream.range(0, melodyData.curve1.size()-1).forEach(i ->{
            if (melodyData.curve1.get(i) != null && melodyData.curve1.get(i + 1) != null) {
                line(i, melodyData.curve1.get(i), i+1, melodyData.curve1.get(i + 1));
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
                        melodyData.curve1.set(i, mouseY);
                    });
                }
                if (m1 > m0) {
                    melodyData.updateCurve(m0 % Config.NUM_OF_MEASURES);
                }
            }
        }
    }

    void processLastMeasure() {
        makeLog("melody");
        if (Config.MELODY_RESETING) {
            getDataModel().shiftMeasure(Config.NUM_OF_MEASURES);
            melodyData.resetCurve();
        }
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
            text(m + " / " + mtotal, 620, 665);
        }
    }

    public void startMusic() {
        if (!inited) {
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
            System.out.println("startMusic() MicrosecondPosition:" + seqencer.getMicrosecondPosition() + " MicrosecondLength:" + seqencer.getMicrosecondLength() + " getFirstMeasure()" + getDataModel().getFirstMeasure());
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

    void resetMusic() {
        if (!isNowPlaying()) {

            initData();
            setTickPosition(0);
            getDataModel().setFirstMeasure(Config.INITIAL_BLANK_MEASURES);
//        makeLog("reset")
        }
    }

    private void makeLog(String action) {
//        if ("melody".equals(action)) cacheLog();
//        if( mDriveServiceHelper == null) mDriveServiceHelper = jamSketchActivity.getDriveServiceHelper();
//        System.out.println("mDriveServiceHelper = " + (mDriveServiceHelper != null));
//        if (mDriveServiceHelper != null) {
//            try {
//                byte[] smfByteArray = melodyData.scc.toWrapper().toMIDIXML().getSMFByteArray();
//                mDriveServiceHelper.uploadFile("test.mid", new ByteArrayInputStream(smfByteArray, 0, smfByteArray.length))
//                        .addOnSuccessListener(fileId -> Log.d(TAG, fileId))
//                        .addOnFailureListener(exception ->
//                                Log.e(TAG, "Couldn't create file.", exception));
//                ;
//            } catch (TransformerException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private void cacheLog() {
//      melodyData.scc.toWrapper().toMIDIXML().writefileAsSMF(midname)
        cachedCurve1.add(melodyData.curve1);
    }

    private void uploadLog() {
// Build a new authorized API client service.
    }

    /**
     * Creates a new file via the Drive REST API.
     */
//    private void createFile() {
//        if (mDriveServiceHelper != null) {
//            Log.d(getClass().getSimpleName(), "Creating a file.");
//
//            mDriveServiceHelper.createFile()
//                    .addOnSuccessListener(fileId -> readFile(fileId))
//                    .addOnFailureListener(exception ->
//                            Log.e(getClass().getSimpleName(), "Couldn't create file.", exception));
//        }
//    }

    /**
     * Retrieves the title and content of a file identified by {@code fileId} and populates the UI.
     */
//    private void readFile(String fileId) {
//        if (mDriveServiceHelper != null) {
//            Log.d(getClass().getSimpleName(), "Reading file " + fileId);
//
//            mDriveServiceHelper.readFile(fileId)
//                    .addOnSuccessListener(nameAndContent -> {
//                        String name = nameAndContent.first;
//                        String content = nameAndContent.second;
//
////                        mFileTitleEditText.setText(name);
////                        mDocContentEditText.setText(content);
//
////                        setReadWriteMode(fileId);
//                    })
//                    .addOnFailureListener(exception ->
//                            Log.e(getClass().getSimpleName(), "Couldn't read file.", exception));
//        }
//    }


    public void showMidiOutChooser() {
        showMidiOutChooser(jamSketchActivity, android.R.layout.simple_list_item_1);
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
    }

    @Override
    public void mouseDragged() {
        if (inited) {
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
