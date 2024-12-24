package jp.kthrlab.jamsketch.view

import controlP5.ControlP5
import jp.crestmuse.cmx.filewrappers.SCCDataSet
import jp.crestmuse.cmx.processing.gui.SimplePianoRoll
import jp.kthrlab.jamsketch.config.AccessibleConfig
import jp.kthrlab.jamsketch.config.IConfigAccessible
import jp.kthrlab.jamsketch.controller.IJamSketchController
import jp.kthrlab.jamsketch.controller.JamSketchClientController
import jp.kthrlab.jamsketch.controller.JamSketchController
import jp.kthrlab.jamsketch.controller.JamSketchServerController
import jp.kthrlab.jamsketch.engine.JamSketchEngine
import jp.kthrlab.jamsketch.music.data.GuideData
import jp.kthrlab.jamsketch.music.data.MusicData
import jp.kthrlab.jamsketch.view.element.drawBGImage
import jp.kthrlab.jamsketch.view.element.drawGuideCurve
import jp.kthrlab.jamsketch.view.element.addParticle
import jp.kthrlab.jamsketch.view.element.drawParticles
import jp.kthrlab.jamsketch.web.ServiceLocator
import processing.core.PApplet
import java.io.File
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

class JamSketch : SimplePianoRoll(), IConfigAccessible {

    companion object {
        const val PACKAGE_NAME_ENGINE: String = "jp.kthrlab.jamsketch.engine"
    }

    override val config = AccessibleConfig.config

    // TODO: Delete comment
    //  fullMeasure -> totalMeasures
    //  mCurrentMeasure -> currentMeasureInTotalMeasures
    val totalMeasures: Int = config.music.num_of_measures * config.music.repeat_times
    val timelineWidth: Int = config.general.view_width - config.general.keyboard_width
    private var currentMeasureInTotalMeasures = 0

    var musicData: MusicData =
        MusicData(
            config.music.midfilename,
            timelineWidth,
            config.music.initial_blank_measures,
            config.music.beats_per_measure,
            config.music.num_of_measures,
            config.music.repeat_times,
            config.music.division,
            config.music.channel_acc,
        )

    var engine: JamSketchEngine = ((Class.forName(PACKAGE_NAME_ENGINE + "." + config.music.jamsketch_engine).getConstructor().newInstance()) as JamSketchEngine).let {
        val target_part: SCCDataSet.Part  = musicData.scc.toDataSet().getFirstPartWithChannel(config.music.channel_acc)
        it.init(musicData.scc, target_part)
        it.resetMelodicOutline()
        it.setFirstMeasure(config.music.initial_blank_measures)
        it
    }

    var controller: IJamSketchController

    var guideData: GuideData? = null

    // Unused
    var username: String = ""

    // A dialogue for JamSketchEventListener
    var panel: JPanel  = JPanel().let {
        val layout = BoxLayout(it, BoxLayout.Y_AXIS)
        it.layout = layout
        it.add(JLabel("The connection is lost."))
        it
    }

    init {
        smfread((musicData.scc as SCCDataSet).midiSequence)

        val part = (musicData.scc as SCCDataSet).getFirstPartWithChannel(config.music.channel_acc)
        dataModel = part.getPianoRollDataModel(config.music.initial_blank_measures,
            config.music.initial_blank_measures + config.music.num_of_measures)
        // init music player ((SMFPlayer)this.musicPlayer[i]).setTickPosition(tick);
        tickPosition = 0

        // init controller
        val listener: JamSketchEventListener =
            JamSketchEventListenerImpl(panel)

        val origController = JamSketchController(
            musicData,
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

        // ControlP5 GUI components
        val p5ctrl = ControlP5(this)
        if (config.general.mode == "client") {
            p5ctrl.addButton("reconnect").setLabel("Reconnect").setPosition(20f, 645f).setSize(120, 40)
        } else {
            p5ctrl.addButton("startMusic").setLabel("Start / Stop").setPosition(20f, 645f).setSize(120, 40)
            p5ctrl.addButton("loadCurve").setLabel("Load").setPosition(300f, 645f).setSize(120, 40)
        }
        p5ctrl.addButton("resetMusic").setLabel("Reset").setPosition(160f, 645f).setSize(120, 40)

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

        // add WindowListener (windowClosing) which calls exit();
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

        // Updating the curve and processing related to it
        if (isUpdatable) {
            updateCurve()
            processOnUpdate()
        }

        // Drawing visual elements
        drawElements()

        // Process for the last measure of each page
        if (isLastMeasure()) processLastMeasure()
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

    /**
     * Processes on update
     */
    private fun processOnUpdate() {
        // Example of adding a visual element
        if (config.general.show_particles) addParticle(this)
    }

    /**
     * Returns true if currentMeasure is the last measure
     */
    private fun isLastMeasure(): Boolean {
        return currentMeasure == config.music.num_of_measures - config.music.num_of_reset_ahead
    }

    /**
     * Draw curves
     */
    private fun drawCurve() {
        strokeWeight(3f)
        stroke(0f, 0f, 255f)
        with(musicData) {
            (0 until curve1.size - 1).forEach { i ->
                if (curve1[i] != null &&  curve1[i+1] != null) {
                    line(
                        (i + config.general.keyboard_width).toFloat(),
                        curve1[i]!!.toFloat(),
                        (i + 1 + config.general.keyboard_width).toFloat(),
                        curve1[i+1]!!.toFloat(),
                    )
                }
            }
        }
    }

    fun updateCurve() {
        // Update music data using the JamSketch controller class
        if(config.general.keyboard_width < pmouseX && config.general.keyboard_width < mouseX) {
            this.controller.updateCurve(
                pmouseX - config.general.keyboard_width,
                mouseX - config.general.keyboard_width,
                mouseY,
                y2notenum(mouseY.toDouble()),
            )
        }
    }

    /**
     * The condition for isUpdatable to be true
     *  !config.on_drag_only                            : on_drag_only == false (It is unclear what happens when on_drag_only == true)
     *  m0 = x2measure(pmouseX.toDouble())              : The number of measures at the start of the drag is greater than or equal to 0
     *  pmouseX < mouseX                                : Dragging from right to left
     *  mouseX > beat2x(currentMeasure, currentBeat) + 10   : The drag end position is ahead of the current position of the performance
     *
     *  "nowDrawing" is removed because it has the same value as mousePressed.
     */
    private val isUpdatable: Boolean
        get() {
//            if ((!config.on_drag_only || nowDrawing) && isInside(mouseX, mouseY)
            if ((!config.general.on_drag_only || mousePressed) && isInside(mouseX, mouseY)
            ) {
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
     * The process to be performed in the last measure on the screen
     */
    private fun processLastMeasure() {
        makeLog("melody")
        // TODO: melody_resetting の意味を確認
        if (config.general.melody_resetting) {
            if (currentMeasureInTotalMeasures < (totalMeasures - config.music.num_of_reset_ahead)) {
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
        if (currentMeasureInTotalMeasures >= totalMeasures) {
            // 生成したメロディを残したまま再スタートするために、measureを戻す
            setPianoRollDataModelFirstMeasure(config.music.initial_blank_measures)

            // Set GuideData start position back to 0
            resetGuideData()
        }

        // SCCGenerator.firstMeasure = num
        engine.setFirstMeasure(dataModel.firstMeasure)
    }

    private fun setPianoRollDataModelFirstMeasure(firstMeasure: Int) {
        dataModel.firstMeasure = firstMeasure
    }

    private fun enhanceCursor() {
        if (config.general.cursor_enhanced) {
            fill(255f, 0f, 0f)
            ellipse(mouseX.toFloat(), mouseY.toFloat(), 10f, 10f)
        }
    }

    private fun drawProgress() {
        if (isNowPlaying) {
            // currentMeasureInTotalMeasures    The current measure number throughout the song (also referenced in processLastMeasure())
            // currentMeasure                   The current measure number on a page
            // dataModel.firstMeasure           Number of starting measures on a page
            // initial_blank_measures           offset of the number of starting measures
            // +1                               Added to start from bar 1 instead of bar 0
            currentMeasureInTotalMeasures =
                (currentMeasure + dataModel.firstMeasure - config.music.initial_blank_measures + 1)
            textSize(32f)
            fill(0f, 0f, 0f)
            text(currentMeasureInTotalMeasures.toString() + " / " + totalMeasures, 460f, 675f)
        }
    }

    private fun resetGuideData() {
        if (guideData != null) {
            guideData!!.fromMeasure = 0
            guideData!!.updateCurveGuideView(0, guideData!!.size)
        }
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
     * ControlP5
     * Reconnect
     */
    fun reconnect() {
        controller.init()
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

    fun makeLog(action: String) {
        jp.kthrlab.jamsketch.view.util.makeLog(action, musicData, config.general.log_dir, this)
    }

}

fun main() {
    PApplet.runSketch(arrayOf("jp.kthrlab.jamsketch.view.JamSketch"), JamSketch())
}
