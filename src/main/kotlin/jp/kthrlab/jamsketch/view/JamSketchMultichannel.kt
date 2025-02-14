package jp.kthrlab.jamsketch.view

import controlP5.ControlP5
import jp.crestmuse.cmx.filewrappers.SCCDataSet
import jp.kthrlab.jamsketch.config.AccessibleConfig
import jp.kthrlab.jamsketch.config.IConfigAccessible
import jp.kthrlab.jamsketch.controller.IJamSketchController
import jp.kthrlab.jamsketch.controller.JamSketchClientController
import jp.kthrlab.jamsketch.controller.JamSketchController
import jp.kthrlab.jamsketch.controller.JamSketchServerController
import jp.kthrlab.jamsketch.engine.IJamSketchEngineMultichannel
import jp.kthrlab.jamsketch.engine.JamSketchEngine
import jp.kthrlab.jamsketch.music.data.GuideData
import jp.kthrlab.jamsketch.music.data.MusicData
import jp.kthrlab.jamsketch.music.data.ObservableMusicData
import jp.kthrlab.jamsketch.music.data.PianoRollDataModelMultiChannel
import jp.kthrlab.jamsketch.view.element.addParticle
import jp.kthrlab.jamsketch.view.element.drawBGImage
import jp.kthrlab.jamsketch.view.element.drawGuideCurve
import jp.kthrlab.jamsketch.view.element.drawParticles
import jp.kthrlab.jamsketch.view.util.addButtons
import jp.kthrlab.jamsketch.view.util.addInstrumentSelector
import jp.kthrlab.jamsketch.web.ServiceLocator
import processing.core.PApplet
import java.io.File
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

//class JamSketchMultichannel: JamSketch() {
class JamSketchMultichannel : SimplePianoRollMultiChannel(), IConfigAccessible {
    companion object {
        const val PACKAGE_NAME_ENGINE: String = "jp.kthrlab.jamsketch.engine"
    }

    override val config = AccessibleConfig.config
    val timelineWidth: Int = config.general.view_width - config.general.keyboard_width
    lateinit var controller: IJamSketchController
    val numOfTotalMeasures: Int = config.music.num_of_measures * config.music.repeat_times
    var guideData: GuideData? = null

    private var currentMeasureInTotalMeasures = 0
    override var currentInstrumentChannelNumber = config.channels[0].number

    var musicData: MusicData =
        MusicData(
            config.music.midfilename,
            timelineWidth,
            config.music.initial_blank_measures,
            config.music.beats_per_measure,
            config.music.num_of_measures,
            config.music.repeat_times,
            config.music.division,
            config.music.channel_gen,
        )
    val numOfMeasuresByDivision = musicData.num_of_measures * musicData.division
    var observableMusicData = ObservableMusicData(
        delegate = musicData,
        onChange = { channel: Int, from: Int, thru: Int, y: Int ->
            println("onChange $channel $from $thru")
            val curveSize = (musicData.channelCurveSet.find { it.first == channel })?.second?.size
            (from .. thru).forEach { x ->
                val position: Int = x * numOfMeasuresByDivision / curveSize!!
                if (position >= 0) {
                    (engine as IJamSketchEngineMultichannel).setMelodicOutline(
                        channel,
                        (position / musicData.division),
                        position % musicData.division,
                        y2notenum(y.toDouble())
                    )
                }
            }
        }
    ).let {
        config.channels.forEach { channel ->
            it.addCurveByChannel(channel.number, arrayOfNulls<Int>(timelineWidth).toMutableList())
        }
        it
    }

    lateinit var p5ctrl: ControlP5

    var engine: JamSketchEngine = ((Class.forName(PACKAGE_NAME_ENGINE + "." + config.music.jamsketch_engine).getConstructor().newInstance()) as IJamSketchEngineMultichannel).let {
        it.init(observableMusicData.scc)
        it.resetMelodicOutline()
        it.setFirstMeasure(config.music.initial_blank_measures)
        it
    }

    // A dialogue for JamSketchEventListener
    var panel: JPanel = JPanel().let {
        val layout = BoxLayout(it, BoxLayout.Y_AXIS)
        it.layout = layout
        it.add(JLabel("The connection is lost."))
        it
    }

    /**
     * The condition for isUpdatable to be true
     *  !config.on_drag_only                            : on_drag_only == false
     *  m0 = x2measure(pmouseX.toDouble())              : The number of measures at the start of the drag is greater than or equal to 0
     *  pmouseX < mouseX                                : Dragging from right to left
     *  mouseX > beat2x(currentMeasure, currentBeat) + 10   : The drag end position is ahead of the current position of the performance
     *
     *  "nowDrawing" is removed because it has the same value as mousePressed.
     */
    protected val isUpdatable: Boolean
        get() {
// !config.general.on_drag_only is commented out because it was checked before this property was referenced.
// mousePressed is commented out because it is always true in mouseDragged().
//            if ((!config.general.on_drag_only || mousePressed) && isInside(mouseX, mouseY)
            if (isInside(mouseX, mouseY)) {
//                val m1 = x2measure(mouseX.toDouble())
                val m0 = x2measure(pmouseX.toDouble())
                return 0 <= m0
                        && pmouseX < mouseX
                        && mouseX > beat2x(currentMeasure, currentBeat) + 10
            } else {
                return false
            }
        }


    /**
     * Role: Used for initial graphical settings such as window size and rendering mode.
     * Invocation Timing: Called first when the PApplet is initialized.
     * Usage Example: Functions like size(), fullScreen(), smooth(), and noSmooth().
     * Constraints: This method cannot contain drawing code like background() or fill().
     */
    override fun settings() {
        super.settings()
        size(config.general.view_width, config.general.view_height)
    }

    /**
     * Role: Used for general initial setup of your sketch, such as setting the background color,
     * initializing variables, and drawing shapes for the first time.
     * Invocation Timing: Called once after settings().
     * Usage Example: Functions like background(), stroke(), fill(), and initializing objects.
     * Constraints: Should not include graphical settings like size() or smooth().
     */
    override fun setup() {
        super.setup()
        showMidiOutChooser()

        // smfread should be called after selecting a MIDI output device.
        smfread((observableMusicData.scc as SCCDataSet).midiSequence)

//        dataModel = (musicData.scc as SCCDataSet)
//            .getFirstPartWithChannel(config.music.channel_gen)
//            .getPianoRollDataModel(config.music.initial_blank_measures,
//            config.music.initial_blank_measures + config.music.num_of_measures)
        dataModel = PianoRollDataModelMultiChannel(
            config.music.initial_blank_measures,
            config.music.initial_blank_measures + config.music.num_of_measures,
//            musicData.scc.division,
            config.music.beats_per_measure,
            config.channels,
            musicData.scc,
        )
//        .let {
//            config.channels.forEach { channel ->
//                it.channelPartSet.add(channel.number, (musicData.scc as SCCDataSet).getFirstPartWithChannel(channel.number))
//            }
//            it
//        }

        // init music player ((SMFPlayer)this.musicPlayer[i]).setTickPosition(tick);
        tickPosition = 0

        // init controller
        val listener: JamSketchEventListener =
            JamSketchEventListenerImpl(panel)

        val origController = JamSketchController(
            observableMusicData,
            engine,
            this::setPianoRollDataModelFirstMeasure,
        )

        controller = when (config.general.mode) {
            "server" -> {
                // Set the controller class to be used when running on the server.
                JamSketchServerController(
                    config.general.host,
                    config.general.port,
                    origController
                )
            }
            "client" -> {
                // Set the controller class to be used when running on the client
                JamSketchClientController(
                    config.general.host,
                    config.general.port,
                    origController,
                    listener
                )
            }
            else -> {
                // スタンドアロンで動かす場合はそのまま
                origController
            }
        }

        val serviceLocator = ServiceLocator.GetInstance()
        serviceLocator.setContoller(controller)

        // ControlP5 GUI components
        p5ctrl = ControlP5(this)
        addButtons(p5ctrl, config.general.mode)
        addInstrumentSelector(p5ctrl, config.channels, this::color)

        // --------------------
        // init GuideData
        // --------------------
        // beat2x depends on dataModel & PApplet.width
        // GuideData needs to be initialized after calling setDataModel & settings()
        guideData =
            if (config.general.show_guide) {
                GuideData(
                    config.music.midfilename,
                    timelineWidth,
                    config.music.initial_blank_measures,
                    config.music.beats_per_measure,
                    config.music.num_of_measures,
                    config.music.repeat_times,
                    config.general.guide_smoothness,
                    config.music.channel_guide,
                    config.general.keyboard_width,
                    this::notenum2y,
                    this::beat2x,
                )
            } else {
                null
            }
    }

    /**
     * --------------------
     * Caution!
     * --------------------
     * Don't write your draw() directly here.
     * When you use JamSketch in your research and need to add your own features,
     * call your drawing method from drawElements().
     * When you need to process during the update, call your method from processOnUpdate().
     */
    override fun draw() {
        super.draw()

        // Manipulating mouseX
        if (config.general.forced_progress) {
            mouseX = beat2x(currentMeasure + config.music.how_in_advance, currentBeat).toInt()
        }

        // Drawing visual elements
        drawElements()

        // Process for the last measure of each page
        if (isLastMeasure()) processLastMeasure()
    }

    override fun mouseDragged() {
        super.mouseDragged()
        mouseMovedOrDragged()
    }

    override fun mouseMoved() {
        super.mouseMoved()
        if (!config.general.on_drag_only) {
            mouseMovedOrDragged()
        }
    }

    protected open fun mouseMovedOrDragged() {
        // Updating the curve and processing related to it
        if (isUpdatable) {
            updateCurve()
            processOnUpdate()
        }
    }

    /**
     * Processes on update
     */
    protected fun processOnUpdate() {
        // Example of adding a visual element
        if (config.general.show_particles) addParticle(this)
    }

    /**
     * Draw visual elements
     */
    private fun drawElements() {
        // Draw basic visual elements
        enhanceCursor()
        drawCurve()
        drawProgress()

        // Draw additional visual elements
        if (config.general.show_guide && guideData != null)  {
            drawGuideCurve(this, guideData!!, config.general.keyboard_width)
        }
        if (config.general.show_bg_image) drawBGImage(this)
        if (config.general.show_particles) drawParticles(this)

    }

    fun updateCurve() {
        println("multichannel updateCurve()")
        // Update music data using the JamSketch controller class
        if (config.general.keyboard_width < pmouseX && config.general.keyboard_width < mouseX) {
            this.controller.updateCurve(
                currentInstrumentChannelNumber,
                pmouseX - config.general.keyboard_width,
                mouseX - config.general.keyboard_width,
                mouseY,
            )
        }
    }

    fun drawCurve() {
        strokeWeight(3f)
        musicData.channelCurveSet.forEach { (channel, curve) ->
            val configChannel = config.channels.find { it.number == channel }
            configChannel?.let {
                with(configChannel.color){
                    stroke(r.toFloat(), g.toFloat(), b.toFloat(),  a.toFloat())
                }
                (0 until curve.size-1).forEach { i ->
                    if (curve[i] != null &&  curve[i+1] != null) {
                        line(
                            (i + config.general.keyboard_width).toFloat(),
                            curve[i]!!.toFloat(),
                            (i + 1 + config.general.keyboard_width).toFloat(),
                            curve[i+1]!!.toFloat(),
                        )
                        blendMode(MULTIPLY)
                    }
                }

            }

        }
        blendMode(1)
    }

    protected fun setPianoRollDataModelFirstMeasure(firstMeasure: Int) {
        dataModel.firstMeasure = firstMeasure
    }

    /**
     * The process to be performed in the last measure on the screen
     */
    protected fun processLastMeasure() {
        makeLog("melody")
        if (config.general.melody_resetting) {
            if (currentMeasureInTotalMeasures < (numOfTotalMeasures - config.music.num_of_reset_ahead)) {
                dataModel.shiftMeasure(config.music.num_of_measures)
            }
            musicData.resetCurve()

            // for Guided
            if (guideData != null) guideData!!.shiftCurveGuideView()

            if (controller is JamSketchServerController) {
                (controller as JamSketchServerController).resetClients()
            }
        }

        // 20241121 add
        // the case when playing up to totalMeasures
        if (currentMeasureInTotalMeasures >= numOfTotalMeasures) {
            // 生成したメロディを残したまま再スタートするために、measureを戻す
            setPianoRollDataModelFirstMeasure(config.music.initial_blank_measures)

            // Set GuideData start position back to 0
            resetGuideData()
        }

        // SCCGenerator.firstMeasure = num
        engine.setFirstMeasure(dataModel.firstMeasure)
    }

    private fun enhanceCursor() {
        if (config.general.cursor_enhanced) {
            fill(255f, 0f, 0f)
            ellipse(mouseX.toFloat(), mouseY.toFloat(), 10f, 10f)
        }
    }

    private fun drawProgress() {
        if (isNowPlaying) {
            // currentMeasureInTotalMeasures    Number of the current measure throughout the song (also referenced in processLastMeasure())
            // currentMeasure                   Number of the current measure on a page
            // dataModel.firstMeasure           Number of starting measures on a page
            // initial_blank_measures           Offset of the number of starting measures
            // +1                               Added to prevent from being displayed as "0th measure" instead of the 1st measure
            currentMeasureInTotalMeasures =
                (currentMeasure + dataModel.firstMeasure - config.music.initial_blank_measures + 1)
            textSize(32f)
            fill(0f, 0f, 0f)
            text(currentMeasureInTotalMeasures.toString() + " / " + numOfTotalMeasures, 460f, 675f)
        }
    }

    /**
     * Returns true if currentMeasure is the last measure
     */
    private fun isLastMeasure(): Boolean {
        return currentMeasure == config.music.num_of_measures - config.music.num_of_reset_ahead
    }

    private fun resetGuideData() {
        if (guideData != null) {
            guideData!!.fromMeasure = 0
            guideData!!.updateCurveGuideView(0, guideData!!.size)
        }
    }

    private fun makeLog(action: String) {
        jp.kthrlab.jamsketch.view.util.makeLog(action, musicData, config.general.log_dir, this)
    }

    /**
     * ControlP5
     * Start/Stop cmx music player
     */
    fun startMusic() {
        if (isNowPlaying) {
            stopMusic()
            makeLog("stop")
        } else {
            playMusic()
            makeLog("play")
        }
    }

    /**
     * ControlP5
     * Reset
     */
    fun resetMusic() {
        // JamSketch操作クラスを使用してリセットする
        controller.reset()

        // Set GuideData start position back to 0
        resetGuideData()

        makeLog("reset")
    }

    /**
     * ControlP5
     * Load saved curve
     */
    fun loadCurve() {
        selectInput("Select a file to process:", "loadFileSelected")
    }

    /**
     * ControlP5 callback
     * Load selected file
     */
    fun loadFileSelected(selection: File) {
        jp.kthrlab.jamsketch.view.util.loadFileSelected(
            selection,
            musicData,
            this,
            config.general.view_width,
            config.general.keyboard_width,
            this::notenum2y
        )
    }

    /**
     * ControlP5 callback
     * Reconnect
     */
    fun reconnect() {
        controller.init()
    }

    /**
     * ControlP5 callback
     * Change instrument
     */
    fun setInstrument(value: Int) {
        currentInstrumentChannelNumber = config.channels.find { channel -> channel.program == value }!!.number
    }

    override fun keyReleased() {
        if (key.equals(" ")) {
            if (isNowPlaying) {
                stopMusic()
            } else {
                tickPosition = 0
                setPianoRollDataModelFirstMeasure(config.music.initial_blank_measures)
                playMusic()
            }
        } else if (key.equals("b")) {
            isNoteVisible = !isNoteVisible
//        } else if (key.equals("u")) {
//            musicData.updateCurve("all")
        }
    }

    override fun exit() {
        println("exit() called.")
        super.exit()
    }

}

fun main() {
    PApplet.runSketch(arrayOf("jp.kthrlab.jamsketch.view.JamSketchMultichannel"), JamSketchMultichannel())
}
