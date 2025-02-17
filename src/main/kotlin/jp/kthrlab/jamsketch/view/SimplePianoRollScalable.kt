package jp.kthrlab.jamsketch.view

import jp.crestmuse.cmx.misc.PianoRoll
import jp.crestmuse.cmx.processing.CMXApplet

open class SimplePianoRollScalable(
    open var musicWidth: Int = 0,    // width before scale
    open var musicHeight: Int = 0,   // height before scale
    basenn: Int = 48,
    var octaveWidth: Double = 210.0,
    var nOctave: Int = 3,
    var keyboardWidth: Double = 100.0,
    var noteR: Int = 255,
    var noteB: Int = 25,
    var noteG: Int = 200,
    var playheadR: Int = 255,
    var playheadB: Int = 0,
    var playheadG: Int = 0,
    var playheadStrokeWeight: Float = 1.0f,
) : CMXApplet() {

    object numOfKeysPerOctave {
        val white = 7
        val black = 5
        val all = white + black // 12
    }

    var basenn: Int = basenn
        set(value) {
            (value % numOfKeysPerOctave.all == 0).let { field = value }
        }

    var isNoteVisible = true
    var dataModel: PianoRoll.DataModel? = null

    override fun setup() {
        this.background(255)
        this.smooth()
    }

    override fun draw() {
        drawPianoRoll()
        drawDataModel()
        drawPlayhead()
    }

    fun drawPianoRoll() {
        this.background(255)
        this.drawKeyboard()
        this.drawLines()
    }

   open fun drawDataModel() {}

    private  fun drawKeyboard() {
        this.strokeWeight(0.5f)
        this.stroke(130)

        for (a in 0..<this.nOctave) {
            this.drawKeyboard(0, (this.octaveWidth * a.toDouble()).toInt())
        }
    }

    private fun drawKeyboard(x: Int, y: Int) {
        this.line(keyboardWidth, (y + 0).toDouble(), keyboardWidth, y.toDouble() + this.octaveWidth)

        // white keys
        for (i in 1..numOfKeysPerOctave.white) {
            this.line(
                x.toFloat(),
                (y + (i.toDouble() * this.octaveWidth / numOfKeysPerOctave.white).toInt()).toFloat(),
                keyboardWidth.toFloat(),
                (y + (i.toDouble() * this.octaveWidth / numOfKeysPerOctave.white).toInt()).toFloat()
            )
        }

        // black keys
        this.fill(0)
        doubleArrayOf(1.0, 3.0, 5.0, 8.0, 10.0).forEach {
            this.rect(
                x.toFloat(),
                (y + (it * this.octaveWidth / numOfKeysPerOctave.all).toInt()).toFloat(),
                (keyboardWidth * 0.6).toFloat(),
                ((this.octaveWidth / numOfKeysPerOctave.all).toInt()).toFloat()
            )
        }

    }


    private fun drawLines() {
        for (a in 0..<this.nOctave) {
            for (b in 1..numOfKeysPerOctave.all) {
                this.stroke(130)
                this.strokeWeight(0.0f)
                val lineWidth = this.octaveWidth / numOfKeysPerOctave.all
                this.line(
                    this.keyboardWidth, a.toDouble() * this.octaveWidth + lineWidth * b.toDouble(),
                    this.musicWidth.toDouble(), a.toDouble() * this.octaveWidth + lineWidth * b.toDouble()
                )
            }
        }

        if (this.dataModel != null) {
            val lengtheach: Double = (this.musicWidth - this.keyboardWidth) / this.dataModel!!.measureNum.toDouble()

            for (i in 0..<this.dataModel!!.measureNum) {
                this.line(
                    ((this.keyboardWidth + i.toDouble() * lengtheach).toInt()).toDouble(),
                    0.0,
                    ((this.keyboardWidth + i.toDouble() * lengtheach).toInt()).toDouble(),
                    nOctave.toDouble() * this.octaveWidth
                )
            }
        }
    }


//    override fun drawNote(measure: Int, beat: Double, duration: Double, notenum: Int, selected: Boolean, data: PianoRoll.DataModel?) {
    fun drawNote(measure: Int, beat: Double, duration: Double, notenum: Int, selected: Boolean, data: PianoRoll.DataModel?) {
        if (this.isNoteVisible) {
            val lenMeas = (this.musicWidth - keyboardWidth) / data!!.measureNum.toDouble()
            val x = this.beat2x(measure, beat)
            val w = duration * lenMeas / data.beatNum.toDouble()
            val y = this.notenum2y(notenum.toDouble())
            fill(color(noteR, noteG, noteB))
            this.rect(x.toFloat(), y.toFloat(), w.toFloat(), octaveWidth.toFloat() / numOfKeysPerOctave.all.toFloat())
        }

    }

//    override fun setKeyboardWidth(keyboardWidth: Double) {
//    fun setKeyboardWidth(keyboardWidth: Double) {
//        this.keyboardWidth = keyboardWidth
//    }

//    override fun getKeyboardWidth(): Double {
//    fun getKeyboardWidth(): Double {
//        return this.keyboardWidth
//    }

    val currentMeasure: Int
        get() {
            return this.dataModel?.tick2measure(this.tickPosition) ?: 0
        }

    val currentBeat: Double
        get() {
            return this.dataModel?.tick2beat(this.tickPosition) ?: 0.0
        }

    protected fun x2measure(x: Double): Int {
//        return this.dataModel?.let { this.x2measure(x) } ?: 0
        val lenMeas: Double = (this.musicWidth - keyboardWidth) / dataModel!!.measureNum.toDouble()
        return ((x - keyboardWidth) / lenMeas).toInt()
    }


    protected fun beat2x(measure: Int, beat: Double): Double {
        this.dataModel?.let {
            val lenMeas = (this.musicWidth - this.keyboardWidth) / it.measureNum.toDouble()
            return this.keyboardWidth + measure.toDouble() * lenMeas + beat * lenMeas / it.beatNum.toDouble()
        } ?: return 0.0
    }

    protected fun y2notenum(y: Double): Double {
        val topnn = this.basenn + numOfKeysPerOctave.all * this.nOctave
        return topnn.toDouble() - y / (this.octaveWidth / numOfKeysPerOctave.all)
    }

    protected fun isInside(x: Int, y: Int, scalePercentage: Float): Boolean {
        return x >= keyboardWidth && x < this.width / scalePercentage && y >= 0 && y.toDouble() < nOctave.toDouble() * octaveWidth
    }

    protected fun notenum2y(nn: Double): Double {
        val topnn = this.basenn + numOfKeysPerOctave.all * this.nOctave
        return this.octaveWidth / numOfKeysPerOctave.all * (topnn.toDouble() - nn - 1.0)
    }

    protected fun drawPlayhead() {
        val measure = this.currentMeasure
        val beat = this.currentBeat

        if (measure >= 0) {
            val x = this.beat2x(measure, beat)
            this.stroke(color(playheadR, playheadG, playheadB))
            this.strokeWeight(playheadStrokeWeight)
            this.line(x.toFloat(), 0.0f, x.toFloat(), (this.octaveWidth * this.nOctave).toFloat())
        }
    }
}
