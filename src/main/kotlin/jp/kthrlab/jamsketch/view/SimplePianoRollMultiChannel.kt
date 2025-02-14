package jp.kthrlab.jamsketch.view

import jp.crestmuse.cmx.processing.gui.SimplePianoRoll
import jp.kthrlab.jamsketch.music.data.PianoRollDataModelMultiChannel

abstract class SimplePianoRollMultiChannel: SimplePianoRoll() {
    protected abstract var currentInstrumentChannelNumber: Int
//    var octaveWidth: Double = 210.0
//    val numOfKeysPerOctave = 12
//    val semitoneWidth: Double
//        get() = octaveWidth / numOfKeysPerOctave

//    override fun beat2x(measure: Int, beat: Double, data: PianoRoll.DataModel?): Double {
//        val lenMeas = (this.width - this.keyboardWidth) / data!!.measureNum.toDouble()
//        return this.keyboardWidth + measure.toDouble() * lenMeas + beat * lenMeas / data.beatNum.toDouble()
//    }

    override fun draw() {
        super.draw()
        drawChannels()
    }

    fun drawChannels() {
        if (isNoteVisible) {
            with (dataModel as PianoRollDataModelMultiChannel) {
                val lenMeas: Double = (width - keyboardWidth) / measureNum
                channels.forEach { channel ->
                    with(channel.color){
                        strokeWeight(1.5f)
                        stroke(r.toFloat(), g.toFloat(), b.toFloat(),  a.toFloat())
                    }
                    val part = getPart(channel.number)
                    part?.noteOnlyList?.forEach { note ->
                        if (note.onset() >= (displaysFromMeasure * beatNum * scc.division).toLong() && note.onset() < (displaysToMeasure * beatNum * scc.division).toLong()) {
                            val measure: Int = (note.onset() / scc.division.toLong() / beatNum.toLong()).toInt()
                            val beat: Double = note.onset().toDouble() / scc.division.toDouble() - (measure * beatNum).toDouble()
                            val duration: Double = (note.offset() - note.onset()).toDouble() / scc.division
                            println("measure=$measure, beat=$beat, duration = ${note.offset()}-${note.onset()} / ${scc.division} = $duration")
                            drawNote(measure - this.displaysFromMeasure, beat, duration, note.notenum(), false, dataModel)
//                            val x = beat2x(measure, beat, this)
//                            val y = notenum2y(note.notenum())
//                            val w = (duration * lenMeas / beatNum)
////                            println("x=$x, y=$y, w=$w, $duration*$lenMeas/$beatNum")
//                            rect(x, y, w, semitoneWidth)
                        }
                    }
                }
            }
        }
    }

}