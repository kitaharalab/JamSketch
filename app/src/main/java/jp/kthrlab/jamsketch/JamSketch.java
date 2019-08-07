package jp.kthrlab.jamsketch;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.stream.IntStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jp.crestmuse.cmx.filewrappers.SCCDataSet;
import jp.crestmuse.cmx.misc.PianoRoll;
import jp.crestmuse.cmx.processing.gui.SimplePianoRoll;
import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.Sequencer;
import jp.kshoji.javax.sound.midi.impl.SequencerImpl;

public class JamSketch extends SimplePianoRoll {

    private MelodyData melodyData;
    private boolean nowDrawing = false;
    private Control control;
    private boolean inited = false;

    @Override
    public void settings() {
        size(1200, 700);
//        showMidiOutChooser();
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


//        p5ctrl.addButton("printSequence")
//                .setLabel("print")
//                .setPosition(440, 645)
//                .setSize(120, 40);

//        initData();

    }

    void icon(boolean value) {
        println("got an event for icon", value);
    }

    void initData() {

        // TODO: add condition 'if (midiouts[0] != null)'

        System.out.println("initData()");
        String filename = Config.MIDFILENAME;
//        println(melodyData.getFullChordProgression());
        melodyData = new MelodyData(filename, width,  this, this);

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
        stroke(0, 0, 255);

        if (inited) {
            drawCurve();

            if (getCurrentMeasure() == Config.NUM_OF_MEASURES - 1) {
                processLastMeasure();
            }

            enhanceCursor();
            drawProgress();
        }

        // test only
//        drawTickPosition();
    }

    void drawCurve() {
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
//        makeLog("melody")
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

    void drawTickPosition() {
        textSize(24);
        fill(0, 0, 0);
        text(getTickPosition(), 600, 680);
    }

    public void startMusic() {

        // TODO: add condition 'if (midiouts[0] != null)'

        if (!inited) {
            initData();
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
//            try {
//                melodyData.scc.toWrapper().println(); //writefileAsSMF(midname);
//            } catch (TransformerException e) {
//                e.printStackTrace();
//            } catch (SAXException e) {
//                e.printStackTrace();
//            }
            stopMusic();

            // add for debug 20190619 fujii
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

//    void printSequence() {
//        Track track = ((SequencerImpl)getSequencer()).getPlayingTrack();
//        for (int i = 0; i < ((SequencerImpl)getSequencer()).getPlayingTrack().size(); i++) {
//            println(i, track.get(i).getTick(), track.get(i).getMessage().getStatus(), track.get(i).getMessage());
//        }
//    }

    // add for debug 20190624 fujii
    @Override
    protected void musicStopped() {
        super.musicStopped();
        System.out.println("musicStopped() isNowPlaying() = " + isNowPlaying());
        Sequencer seqencer = getSequencer();
        System.out.println("musicStopped() MicrosecondPosition:" + seqencer.getMicrosecondPosition() + " MicrosecondLength:" + seqencer.getMicrosecondLength());
    }

    public void showMidiOutChooser() {
        showMidiOutChooser(JamSketchActivity.getMyContext(), android.R.layout.simple_list_item_1);

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
        storeCursorPosition();
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
