package jp.kthrlab.jamsketch

import jp.crestmuse.cmx.filewrappers.SCCDataSet
import jp.crestmuse.cmx.inference.MusicCalculator
import jp.crestmuse.cmx.inference.MusicRepresentation
import jp.crestmuse.cmx.misc.PianoRoll
import jp.crestmuse.cmx.processing.CMXController
import jp.crestmuse.cmx.processing.gui.SimplePianoRoll
import jp.kshoji.javax.sound.midi.impl.SequencerImpl


class SCCGenerator (
    private var target_part: SCCDataSet.Part,
    private var sccdiv: Int,
    private var curveLayer: String,
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
        //      def sccdiv = scc.getDivision()
        //def firstMeasure = pianoroll.getDataModel().getFirstMeasure()
        var e = mr.getMusicElement(layer, measure, tick)
        if (!e.rest() && !e.tiedFromPrevious()) {
            //def curvevalue = curve2[measure * CFG.DIVISION + tick]
            var curvevalue =
            mr.getMusicElement(curveLayer, measure, tick).getMostLikely()
            if (curvevalue != null) {
                var notenum = getNoteNum(e.getMostLikely() as Int, curvevalue as Double)
                var duration = e.duration() * sccdiv /
                        (Config.DIVISION / Config.BEATS_PER_MEASURE)
                var onset = ((firstMeasure + measure) * Config.DIVISION + tick) * sccdiv /
                        (Config.DIVISION / Config.BEATS_PER_MEASURE)

//                println("onset = ((${firstMeasure} + ${measure}) * ${Config.DIVISION} + ${tick}) * ${sccdiv} / (${Config.DIVISION} / ${Config.BEATS_PER_MEASURE}) = ${onset}")
                if (onset > CMXController.getInstance().getTickPosition()) {
                    synchronized(this) {
                        //	    def oldnotes =
                        //	      SCCUtils.getNotesBetween(target_part, onset,
                        //				       onset+duration, sccdiv, true, true)
                        //data.target_part.getNotesBetween2(onset, onset+duration)
                        //	      target_part.remove(oldnotes)
                        // edit 2020.03.04
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
                            if (note.onset() < onset+duration &&
                                    onset+duration < note.offset()) {
                                //		  note.setOnset(onset+duration)
                            }
                        }
                        target_part.addNoteElement(onset.toLong(), (onset+duration).toLong(), notenum,
                                100, 100)
                    }
                    (cmx!!.getSequencer() as SequencerImpl).refreshPlayingTrack()
                }
            }
        }

//        if (Config.EXPRESSION) {
//            var fromTick = (firstMeasure + measure) * Config.BEATS_PER_MEASURE *
//                    Config.DIVISION
//            var thruTick = fromTick + Config.BEATS_PER_MEASURE * Config.DIVISION
//            expgen.execute(fromTick, thruTick, Config.DIVISION)
//        }
    }

//    @CompileStatic
    fun getNoteNum(notename: Int, neighbor: Double): Int {
        var best = 0
        for (i in 0..11) {
            var notenum = i * 12 + notename
            if (Math.abs(notenum - neighbor) < Math.abs(best - neighbor))
                best = notenum
        }
        return best
    }
}
