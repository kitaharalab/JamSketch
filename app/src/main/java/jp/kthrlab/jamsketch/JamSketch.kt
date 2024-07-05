package jp.kthrlab.jamsketch

import android.util.Log
import androidx.fragment.app.FragmentActivity
import jp.crestmuse.cmx.processing.CMXController
import jp.crestmuse.cmx.processing.DeviceNotAvailableException
import jp.crestmuse.cmx.processing.gui.SimplePianoRoll
import jp.crestmuse.cmx.sound.SoundUtils
import jp.kshoji.javax.sound.midi.InvalidMidiDataException
import jp.kshoji.javax.sound.midi.MidiUnavailableException
import jp.kshoji.javax.sound.midi.impl.SequencerImpl
import org.xml.sax.SAXException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Calendar
import java.util.Locale
import java.util.stream.IntStream
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerException

class JamSketch     //    private Checkbox sendGranted;
    (private val jamSketchActivity: JamSketchActivity) : SimplePianoRoll() {
    private var melodyData: MelodyData2? = null
    private var nowDrawing = false
    private var control: Control? = null
    private var initiated = false
    private var cloudFunctionsHelper: CloudFunctionsHelper? = null
    @JvmField
    var scale = 1.0f
    private val margin = 10
    private var buttonWidth = 0
    private var buttonHeight = 0
    private var buttonY = 0f
    private var myCurrentMeasure = 0
    private var fullMeasure = 0
    override fun settings() {
//        pixelDensity = (int)displayDensity;
        scale = displayDensity
        if (displayWidth >= displayHeight) {
            if ((600 * scale).toInt() > displayWidth) {
                scale = displayWidth / 600.0f
            }
            if ((350 * scale).toInt() > displayHeight) {
                scale = displayHeight / 350.0f
            }
        } else {
            if ((600 * scale).toInt() > displayHeight) {
                scale = displayHeight / 600.0f
            }
            if ((350 * scale).toInt() > displayWidth) {
                scale = displayWidth / 350.0f
            }
        }
        size((600 * scale).toInt(), (350 * scale).toInt())
        setOctaveWidth(height * 0.3)
        keyboardWidth = (width / (Config.NUM_OF_MEASURES + 1)).toDouble()
    }

    override fun setup() {
        super.setup()
        orientation(LANDSCAPE)
        buttonWidth = (width - margin * 9) / 8
        buttonHeight = height / 10 - margin * 2
        buttonY = (height * 9 / 10 + margin).toFloat()
        control = Control(this)
        control!!.addButton("startMusic")
            .setLabel("Start")
            .setPosition(margin.toFloat(), buttonY)
            .setSize(buttonWidth, buttonHeight)
        control!!.addButton("stopPlayMusic").setLabel("Stop")
            .setPosition((margin * 2 + buttonWidth).toFloat(), buttonY)
            .setSize(buttonWidth, buttonHeight)
        control!!.addButton("resetMusic").setLabel("Reset")
            .setPosition((margin * 3 + buttonWidth * 2).toFloat(), buttonY)
            .setSize(buttonWidth, buttonHeight)
        //        p5ctrl.addButton("loadCurve").
//                setLabel("Load").setPosition(300, 645).setSize(120, 40);
        control!!.addButton("showMidiOutChooser").setLabel("MidiOut")
            .setPosition((margin * 4 + buttonWidth * 3).toFloat(), buttonY)
            .setSize(buttonWidth, buttonHeight)

//        sendGranted = control.addCheckbox("sendGranted");
//        sendGranted.setLabel("Send melody to kthrlab.jp")
//                .setLabelSize(130, 50)
//                .setPosition(800, 655)
//                .setSize(20, 20);
        control!!.addButton("sendGood").setLabel("Good↑")
            .setPosition((margin * 7 + buttonWidth * 6).toFloat(), buttonY)
            .setSize(buttonWidth, buttonHeight)
        control!!.addButton("sendBad").setLabel("Bad↓")
            .setPosition((margin * 8 + buttonWidth * 7).toFloat(), buttonY)
            .setSize(buttonWidth, buttonHeight)
    }

    //    void icon(boolean value) {
    //        println("got an event for icon", value);
    //    }
    fun initData() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "initData()")
        }
        val filename = Config.getMidiFileName()
        melodyData = MelodyData2(Config.getMidiFileName(), width, this, this, Config())
        try {
            smfread(melodyData!!.scc.midiSequence)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: TransformerException) {
            e.printStackTrace()
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: InvalidMidiDataException) {
            e.printStackTrace()
        }
        val part = melodyData!!.scc.getFirstPartWithChannel(1)
        dataModel = part.getPianoRollDataModel(
            Config.INITIAL_BLANK_MEASURES,
            Config.INITIAL_BLANK_MEASURES + Config.NUM_OF_MEASURES
        )
        fullMeasure = dataModel.measureNum * Config.REPEAT_TIMES
        initiated = true
    }

    override fun draw() {
        super.draw()
        strokeWeight(3f)
        if (initiated) {
//            println("State = " + ((SequencerImpl)getSequencer()).getState());
            drawCurve()
            drawProgress()
            if (getCurrentMeasure() == Config.NUM_OF_MEASURES - Config.NUM_OF_RESET_AHEAD)
                processLastMeasure()
            enhanceCursor()
        }
    }

    fun drawCurve() {
        stroke(0f, 0f, 255f)
        IntStream.range(0, melodyData!!.curve1!!.size - 1).forEach { i: Int ->
            if (melodyData!!.curve1!![i] != null && melodyData!!.curve1!![i + 1] != null) {
                line(
                    i.toFloat().toDouble(),
                    melodyData!!.curve1!![i]!!.toDouble(),
                    (i + 1).toFloat().toDouble(),
                    melodyData!!.curve1!![i + 1]!!.toDouble()
                )
            }
        }
    }

    fun processLastMeasure() {
        makeLog("melody")
        if (Config.MELODY_RESETING) {
            if (myCurrentMeasure < fullMeasure - Config.NUM_OF_RESET_AHEAD) {
                dataModel.shiftMeasure(Config.NUM_OF_MEASURES)
            }
            melodyData!!.resetCurve()
        }
        melodyData!!.engine.setFirstMeasure(dataModel.firstMeasure)
    }

    fun enhanceCursor() {
        if (Config.CURSOR_ENHANCED) {
            fill(255f, 0f, 0f)
            ellipse(mouseX.toFloat(), mouseY.toFloat(), 10f, 10f)
        }
    }

    fun drawProgress() {
        if (isNowPlaying) {
            val dataModel = dataModel
            myCurrentMeasure = getCurrentMeasure() + dataModel.firstMeasure -
                    Config.INITIAL_BLANK_MEASURES + 1
            textSize(16 * scale)
            fill(0f, 0f, 0f)
            text(
                "$myCurrentMeasure / $fullMeasure",
                (margin * 6 + buttonWidth * 5).toFloat(),
                buttonY + buttonHeight / 2
            )
        }
    }

    fun startMusic() {
        if (!initiated) {
            try {
                initData()
                playMusic()
            } catch (e: DeviceNotAvailableException) {
                e.printStackTrace()
                showMidiOutChooserAndPlay()
            }
        } else {
            if (!isNowPlaying) {
                playMusic()
            }
        }
    }

    fun stopPlayMusic() {
        println("stopPlayMusic>>> ")
        if (isNowPlaying) {
            stopMusic()

            // add 20190619 fujii
            (sequencer as SequencerImpl).loopStartPoint = tickPosition
        }
    }

    fun resetMusic() {
        if (!isNowPlaying) {
            initData()
            resetData()
            resetSequencer()
            println("reset")
            //            makeLog("reset");
        }
    }

    private fun resetData() {
        dataModel.firstMeasure = Config.INITIAL_BLANK_MEASURES
        melodyData!!.engine.setFirstMeasure(dataModel.firstMeasure)
        if (BuildConfig.DEBUG) {
            println("resetData>>> getCurrentMeasure() = " + getCurrentMeasure() + ", getCurrentBeat() = " + currentBeat)
        }
    }

    private fun resetSequencer() {
        tickPosition = 0
        val seqencer = sequencer
        seqencer.loopStartPoint = 0
        if (seqencer is SequencerImpl) seqencer.refreshPlayingTrack()
        if (BuildConfig.DEBUG) {
            println("resetSequencer>>> MicrosecondPosition:" + seqencer.microsecondPosition + " MicrosecondLength:" + seqencer.microsecondLength + " getFirstMeasure():" + dataModel.firstMeasure)
        }
    }

    override fun musicStopped() {
        super.musicStopped()
        if (BuildConfig.DEBUG) {
            println("musicStopped>>> StoppedgetMicrosecondPosition() = $microsecondPosition")
            println("musicStopped>>> getSequencer().getMicrosecondLength() = " + sequencer.microsecondLength)
            Arrays.stream(Thread.currentThread().stackTrace)
                .forEach { x: StackTraceElement? -> println(x) }
        }
        if (microsecondPosition >= sequencer.microsecondLength) {
            resetMusic()
        }
    }

    fun sendGood() {
        makeLog("good")
    }

    fun sendBad() {
        makeLog("bad")
    }

    private fun makeLog(action: String) {
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH-mm-ss_E", Locale.ENGLISH)
        val logname = sdf.format(Calendar.getInstance().time)
        if ("melody" == action) {

            // Send logs
//                if (sendGranted.checked()) {
            // midi
            uploadFile(
                logname + "_melody.mid",
                melodyData,
                "audio/midi"
            )

            // sccxml
            uploadFile(
                logname + "_melody.sccxml",
                melodyData,
                "text/xml"
            )

            // json
            uploadFile(
                logname + "_curve.json",
                melodyData,
                "application/json"
            )

//                        // screenshot
//                        ByteArrayOutputStream outputStreamImg = new ByteArrayOutputStream();
//                        get();
//                        save("test");
//                }
        } else {
            uploadFile(
                logname + "_" + action + ".txt",
                action,
                "text/plain"
            )
        }
    }

    private fun uploadFile(fileName: String, melodyData: MelodyData2?, mimeType: String) {
        if (cloudFunctionsHelper == null) cloudFunctionsHelper = CloudFunctionsHelper()
        when (mimeType) {
            "audio/midi" -> {
                cloudFunctionsHelper!!.uploadScc(fileName, melodyData!!, mimeType)
                    .addOnSuccessListener { fileId: String? ->
                        if (BuildConfig.DEBUG) Log.d(
                            TAG,
                            fileId!!
                        )
                    }
                    .addOnFailureListener { exception: Exception? ->
                        if (BuildConfig.DEBUG) Log.e(
                            TAG,
                            "Couldn't create file.",
                            exception
                        )
                    }
            }
            "text/xml" -> {
                cloudFunctionsHelper!!.uploadSccxml(fileName, melodyData!!, mimeType)
                    .addOnSuccessListener { fileId: String? ->
                        if (BuildConfig.DEBUG) Log.d(
                            TAG,
                            fileId!!
                        )
                    }
                    .addOnFailureListener { exception: Exception? ->
                        if (BuildConfig.DEBUG) Log.e(
                            TAG,
                            "Couldn't create file.",
                            exception
                        )
                    }
            }
            "application/json" -> {
                cloudFunctionsHelper!!.uploadJson(fileName, melodyData!!, mimeType)
                    .addOnSuccessListener { fileId: String? ->
                        if (BuildConfig.DEBUG) Log.d(
                            TAG,
                            fileId!!
                        )
                    }
                    .addOnFailureListener { exception: Exception? ->
                        if (BuildConfig.DEBUG) Log.e(
                            TAG,
                            "Couldn't create file.",
                            exception
                        )
                    }
            }
        }
    }

    private fun uploadFile(fileName: String, text: String, mimeType: String) {
        if (cloudFunctionsHelper == null) cloudFunctionsHelper = CloudFunctionsHelper()
        cloudFunctionsHelper!!.uploadText(fileName, text, mimeType)
            .addOnSuccessListener { fileId: String? -> if (BuildConfig.DEBUG) Log.d(TAG, fileId!!) }
            .addOnFailureListener { exception: Exception? ->
                if (BuildConfig.DEBUG) Log.e(
                    TAG,
                    "Couldn't create file.",
                    exception
                )
            }
    }

    fun showMidiOutChooser() {
        if (!hasMidiOutDeviceInfo()) openGooglePlay() else showMidiOutChooser(
            (activity as FragmentActivity).supportFragmentManager,
            android.R.layout.simple_list_item_1
        )
    }

    private fun showMidiOutChooserAndPlay() {
        if (!hasMidiOutDeviceInfo()) openGooglePlay() else ChooseMidioutNPlayDialogFragment {
            initData()
            Unit
        }
            .setCMXController(CMXController.getInstance())
            .setLayout(android.R.layout.simple_list_item_1)
            .show(
                (activity as FragmentActivity).supportFragmentManager,
                "MidiOutChooser"
            )
    }

    private fun hasMidiOutDeviceInfo(): Boolean {
        return try {
            SoundUtils.getMidiOutDeviceInfo().size > 0
        } catch (e: MidiUnavailableException) {
            e.printStackTrace()
            false
        }
    }

    private fun openGooglePlay() {
        OpenGooglePlayDialogFragment()
            .show(
                (activity as FragmentActivity).supportFragmentManager,
                "OpenGooglePlay"
            )
    }

    fun loadCurve() {}

    override fun mousePressed() {
        nowDrawing = true
    }

    override fun mouseReleased() {
        nowDrawing = false
        if (isInside(mouseX, mouseY)) {
            if (melodyData != null && !melodyData!!.engine.automaticUpdate()) {
                if (BuildConfig.DEBUG) println("mouseReleased() before outlineUpdated")
                melodyData!!.engine.outlineUpdated(
                    x2measure(mouseX.toDouble()) % Config.NUM_OF_MEASURES,
                    Config.getDivision() - 1
                )
                if (BuildConfig.DEBUG) println("mouseReleased() after outlineUpdated")
            }
        }
    }

    override fun mouseDragged() {
        if (initiated && isNowPlaying) {
            if (pmouseX < mouseX &&
                mouseX > beat2x(
                    currentMeasure,
                    currentBeat
                ) + ticksPerBeat / Config.COMPOSABLE_TICKS_DIV
            ) {
                if ((Config.ON_DRAG_ONLY || nowDrawing) &&
                    isInside(mouseX, mouseY) && sequencer.tickPosition < sequencer.tickLength
                ) {
                    val m1 = x2measure(mouseX.toDouble())
                    val m0 = x2measure(pmouseX.toDouble())
                    if (0 <= m0) {
                        IntStream.rangeClosed(pmouseX, mouseX).forEach { i: Int ->
                            // store cursor position
                            melodyData!!.curve1!![i] = mouseY
                        }
                        if (BuildConfig.DEBUG) println("--------- beginning of mouseDragged()::outlineUpdated")
                        melodyData!!.updateCurve(pmouseX, mouseX)
                        if (BuildConfig.DEBUG) println("--------- end of mouseDragged()::outlineUpdated")
                    }
                }
            }
        }
    } //    @Override

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

    override fun exit() {
        super.exit()

    }

    companion object {
        private const val TAG = "JamSketch"
    }
}