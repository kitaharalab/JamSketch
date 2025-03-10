package jp.kthrlab.jamsketch.view.util

import jp.crestmuse.cmx.processing.CMXController
import jp.kthrlab.jamsketch.music.data.MusicData
import javax.sound.midi.MidiSystem
import javax.sound.midi.Synthesizer

val baseNotes = mapOf(
    "C" to 0, "C#" to 1, "D" to 2, "D#" to 3, "E" to 4,
    "F" to 5, "F#" to 6, "G" to 7, "G#" to 8, "A" to 9,
    "A#" to 10, "B" to 11
)

/**
 * Returns the international MIDI note number
 */
fun getNoteNumber(note: String, octave: Int): Int {
    val nn = baseNotes[note] ?: throw IllegalArgumentException("Invalid note")
    // The note number of C in octave -1 is 0
    return (octave + 1) * 12 + nn
}

fun sendAllNotesOff(musicData: MusicData) {
    val cmx = CMXController.getInstance()
    musicData.channelCurveSet.forEach { channel ->
        val channelPart = musicData.scc.toDataSet().getFirstPartWithChannel(channel.first)//.addNoteElement()
        val tickPosition = cmx.tickPosition

        // from note 0 t0 127
        musicData.channelCurveSet.forEach { channel ->
            (0..127).forEach { nn ->
                val noteElement = channelPart.addNoteElement(tickPosition, tickPosition, nn, 0, 0)
                println("addNoteElement($tickPosition, $tickPosition, $nn, 0, 0)")
                channelPart.remove(noteElement)
            }
        }
    }
}