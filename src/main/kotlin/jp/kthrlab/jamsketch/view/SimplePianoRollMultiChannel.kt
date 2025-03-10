package jp.kthrlab.jamsketch.view

import jp.kthrlab.jamsketch.music.data.PianoRollDataModelMultiChannel

abstract class SimplePianoRollMultiChannel: SimplePianoRollScalable() {
    protected abstract var currentInstrumentChannelNumber: Int

    override fun drawDataModel() {
        drawChannels()
    }

    fun drawChannels() {
        if (isNoteVisible) {
            with (dataModel as PianoRollDataModelMultiChannel) {
                channels.forEach { channel ->
                    with(channel.color){
                        strokeWeight(1.5f)
                        stroke(r.toFloat(), g.toFloat(), b.toFloat(),  a.toFloat())
                    }
                    val part = getPart(channel.channel_number)
                    part?.noteOnlyList?.forEach { note ->
                        if (note.onset() >= (displaysFromMeasure * beatNum * scc.division).toLong() && note.onset() < (displaysToMeasure * beatNum * scc.division).toLong()) {
                            val measure: Int = (note.onset() / scc.division.toLong() / beatNum.toLong()).toInt()
                            val beat: Double = note.onset().toDouble() / scc.division.toDouble() - (measure * beatNum).toDouble()
                            val duration: Double = (note.offset() - note.onset()).toDouble() / scc.division
//                            println("measure=$measure, beat=$beat, duration = ${note.offset()}-${note.onset()} / ${scc.division} = $duration")
                            drawNote(measure - this.displaysFromMeasure, beat, duration, note.notenum(), false, dataModel)
                        }
                    }
                    blendMode(MULTIPLY)
                }
                blendMode(1)
            }
        }
    }

}