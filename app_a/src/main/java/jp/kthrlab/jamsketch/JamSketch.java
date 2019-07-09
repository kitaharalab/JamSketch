package jp.kthrlab.jamsketch;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.stream.IntStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import controlP5.ControlP5;
import jp.crestmuse.cmx.filewrappers.SCCDataSet;
import jp.crestmuse.cmx.misc.PianoRoll;
import jp.crestmuse.cmx.processing.gui.SimplePianoRoll;
import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.Sequencer;
import jp.kshoji.javax.sound.midi.impl.SequencerImpl;

public class JamSketch extends SimplePianoRoll {

    private MelodyData melodyData;
    private boolean nowDrawing = false;
    private String status ="aaa";
    private ControlP5 p5ctrl;
    private Control control;

//    Button button;
//    Layout layout;
//    final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void settings() {
        size(1200, 700);
//        showMidiOutChooser();
    }

    @Override
    public void setup() {
        super.setup();
        status = "setup()";

        orientation(LANDSCAPE);

//        button = new Button(getActivity());
//        button.setX(440);
//        button.setY(645);
//        button.setWidth(120);
//        button.setHeight(40);
//        button.setText("BUTTON");
//        handler.post(() -> {
//            ((FrameLayout)getWindow().getDecorView().getRootView()).addView(button);
//        }) ;
        control = new Control(this);
        control.addButton("startMusic")
                .setLabel("Start")
                .setPosition(20, 645)
                .setSize(120, 40);

        p5ctrl = new ControlP5(this);
//        p5ctrl.addButton("startMusic").
//                setLabel("Start").setPosition(20, 645).setSize(120, 40);
        p5ctrl.addButton("stopPlayMusic").
//                activateBy(ControlP5.PRESS).
                setLabel("Stop").setPosition(160, 645).setSize(120, 40);
//        p5ctrl.addButton("resetMusic").
//                setLabel("Reset").setPosition(160, 645).setSize(120, 40);
//        p5ctrl.addButton("loadCurve").
//                setLabel("Load").setPosition(300, 645).setSize(120, 40);
        p5ctrl.addButton("showMidiOutChooser").
                setLabel("MidiOut").setPosition(300, 645).setSize(120, 40);


//        p5ctrl.addButton("printSequence")
//                .setLabel("print")
//                .setPosition(440, 645)
//                .setSize(120, 40);

//        p5ctrl.addIcon("icon",10)
//                .setPosition(100,100)
//                .setSize(70,50)
//                .setRoundedCorners(20)
//                .setFont(createFont("fontawesome-webfont.ttf", 40))
//                .setFontIcons(0x00f205, 0x00f204)
//                .setColorBackground(color(255,100))
//                .hideBackground()
//                .setSwitch(true);

        initData();

    }

    void icon(boolean value) {
        println("got an event for icon", value);
    }

    void initData() {
        System.out.println("initData()");
        status = "initData()";
        String filename = Config.MIDFILENAME;
//        try {
////            filename = getClass().getClassLoader().getResource(Config.MIDFILENAME).toURI().getPath();
////            System.out.println(System.getProperty("java.class.path"));
//            URL url = getClass().getClassLoader().getResource(Config.MIDFILENAME);
//            filename = getClass().getResource(Config.MIDFILENAME).toURI().getPath();
//            System.out.println(filename);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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

    }

    @Override
    public void draw() {
        super.draw();
        strokeWeight(3);
        stroke(0, 0, 255);

        if (melodyData != null) {
            drawCurve();

            if (getCurrentMeasure() == Config.NUM_OF_MEASURES - 1) {
                processLastMeasure();
            }

            enhanceCursor();
//            drawProgress();
        }

//        control.draw();
        // test only
        drawStatus();
//        if (button != null)
//            button.setVisibility(View.VISIBLE);
    }

    void drawCurve() {
//        line(pmouseX, pmouseY, mouseX, mouseY);
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
            text(m + " / " + mtotal, 600, 640);
        }
    }

    void drawStatus() {
        textSize(24);
        fill(0, 0, 0);
        text(getTickPosition(), 600, 660);
    }

    public void startMusic() {
        status = "playMusic() called. isNowPlaying() = " + isNowPlaying();

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
            status = "playMusic()";
        }
    }

    public void stopPlayMusic() {
        status = "stopPlayMusic() called. isNowPlaying() = " + isNowPlaying();
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
            status = "stopMusic(). isNowPlaying() = " + isNowPlaying();
        }
    }

    void resetMusic() {
        initData();
        setTickPosition(0);
        getDataModel().setFirstMeasure(Config.INITIAL_BLANK_MEASURES);
//        makeLog("reset")
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
        status = "showMidiOutChooser()";
        showMidiOutChooser(JamSketchActivity.getMyContext(), android.R.layout.simple_list_item_1);

    }

    public void loadCurve() {
        status = "loadCurve() called.";
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
