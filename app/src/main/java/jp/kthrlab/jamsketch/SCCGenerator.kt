package jp.kthrlab.jamsketch

import jp.crestmuse.cmx.filewrappers.SCCDataSet
import jp.crestmuse.cmx.inference.MusicCalculator
import jp.crestmuse.cmx.inference.MusicRepresentation
import jp.crestmuse.cmx.processing.CMXController
import jp.kshoji.javax.sound.midi.Sequencer
import jp.kshoji.javax.sound.midi.impl.SequencerImpl


class SCCGenerator (
    private var target_part: SCCDataSet.Part,
    private var sccdiv: Int,
    private var curveLayer: String,
    private var expgen: ExpressionGenerator,
    private var CFG: Config
) : MusicCalculator {

    var cmx: CMXController? = null
        get() = field
        set(value) {
            field  = value
        }

    companion object {
        var firstMeasure = Config.INITIAL_BLANK_MEASURES
    }

    override fun updated(measure: Int, tick: Int, layer: String, mr: MusicRepresentation) {

        synchronized(this) {
            if (BuildConfig.DEBUG) println("--------- beginning of SCCGenerator::updated(${measure}, ${tick}, ${layer}, ${mr})")

            val e = mr.getMusicElement(layer, measure, tick)
            if (!e.rest() && !e.tiedFromPrevious()) {
                //def curvevalue = curve2[measure * CFG.DIVISION + tick]
                val curvevalue = mr.getMusicElement(curveLayer, measure, tick).getMostLikely()
                if (curvevalue != null) {
                    val notenum =  if (Config.JAMSKETCH_ENGINE == "jp.kthrlab.jamsketch.JamSketchEngineTF1"){
                        e.mostLikely as Int + Config.TF_NOTE_NUM_START
                    } else e.mostLikely as Int

                    val duration = e.duration() * sccdiv /
                            (Config.getDivision() / Config.BEATS_PER_MEASURE)
                    val onset = ((firstMeasure + measure) * Config.getDivision() + tick) * sccdiv /
                            (Config.getDivision() / Config.BEATS_PER_MEASURE)

                    if (onset > CMXController.getInstance().getTickPosition()) {
                        target_part.noteList.forEach { note ->
                            if (note.onset() < onset && onset <= note.offset()) {
                                target_part.remove(note)
                                target_part.addNoteElement(note.onset(), (onset-1).toLong(),
                                        note.notenum(),
                                        note.velocity(),
                                        note.offVelocity())
                            }
                            if (onset <= note.onset() && note.offset() <= onset+duration) {
                                target_part.remove(note)
                            }
//                            if (note.onset() < onset+duration &&
//                                    onset+duration < note.offset()) {
//                                //		  note.setOnset(onset+duration)
//                            }
                        }
                        target_part.addNoteElement(
                            onset.toLong(),
                            (onset+duration).toLong(),
                            notenum,
                            100,
                            100
                        )

                        var sequencer:Sequencer = cmx!!.sequencer
                        if (sequencer is SequencerImpl) sequencer.refreshPlayingTrack()
                    }
                }
            }
        }

        if (Config.EXPRESSION) {
            val fromTick = (firstMeasure + measure) * Config.BEATS_PER_MEASURE *
                    Config.getDivision()
            val thruTick = fromTick + Config.BEATS_PER_MEASURE * Config.getDivision()
            expgen.execute(fromTick, thruTick, Config.getDivision())
        }
    }
}
