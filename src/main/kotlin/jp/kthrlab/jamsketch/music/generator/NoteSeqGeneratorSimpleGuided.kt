package jp.kthrlab.jamsketch.music.generator

import jp.crestmuse.cmx.filewrappers.SCC
import jp.crestmuse.cmx.filewrappers.SCCDataSet
import jp.crestmuse.cmx.inference.MusicCalculator
import jp.crestmuse.cmx.inference.MusicRepresentation
import kotlin.math.ln

// TODO: NoteSeqGeneratorSimpleの派生クラスにしたい
class NoteSeqGeneratorSimpleGuided(
    private var noteLayer: String,
    private var chordLayer: String,
    private var guidepart: SCCDataSet.Part,
    private var mr: MusicRepresentation,
    private var initial: Int,
    private var beatsPerMeas: Int
) :
    MusicCalculator {
//    var guidepart: SCC.Part = guidepart
    private var initialBlank: Int = initial * beatsPerMeas

    private var unigram1: MutableList<Double> = mutableListOf()
    private var bigram: MutableList<MutableList<Double>> = mutableListOf()
    private var w1: Double = 1.0
    private var w2: Double = 1.0

    init {
        makeBigram()
        decideRhythm()
    }

    fun makeBigram() {
        unigram1 = MutableList(12) {0.0}
        bigram = MutableList(12) { MutableList(12) { 0.0 } }
        println(unigram1)
        println(bigram)
        var prev: Int? = null
        guidepart.noteList.forEach { note ->
            try {
                val nn: Int = (note as SCC.Note).notenum() % 12
                if (prev == null) {
                    this.unigram1[nn] += 1.0
                } else {
                    this.bigram[prev][nn] += 1.0
                }
            } catch (e: UnsupportedOperationException) {
            }
        }
        unigram1 = unigram1.map { it / unigram1.sum() } as MutableList<Double>
        bigram.forEachIndexed { index, doubles ->
            bigram[index] = bigram[index].map { it / bigram[index].sum() } as MutableList<Double>
        }
    }

    fun decideRhythm() {
        // TODO: how to deal with rest?
        guidepart.noteList.forEach { note ->
            val onset = note.onset(480) / 480 - this.initialBlank
            val i1 = (onset * this.mr.division / this.beatsPerMeas) as Int
            val offset = note.offset(480) / 480 - this.initialBlank
            val i2: Int = (offset * this.mr.division / this.beatsPerMeas) as Int
            if (i1 < this.mr.measureNum * this.mr.getDivision()) {
                this.mr.getMusicElement(this.noteLayer, 0, i1).setTiedFromPrevious(false)
            }

            for (i in IntRange(i1 + 1, i2)) {
                if (i < this.mr.measureNum * this.mr.division) {
                    this.mr.getMusicElement(this.noteLayer, 0, i).setTiedFromPrevious(true)
                }
            }
        }
    }

    override fun updated(measure: Int, tick: Int, layer: String, mr: MusicRepresentation) {
        val e_curve = mr.getMusicElement(layer, measure, tick)
        val value = e_curve.mostLikely as Double
        val score =  mutableListOf<Double>()
        if (!value.isNaN()) {
            val e_melody = mr.getMusicElement(noteLayer, measure, tick)
            val value12 = value - (value / 12).toInt() * 12
            (0..11).forEach {
                val simil = -ln((value12 - it) * (value12 - it))
                val prev = e_melody.prev()
                val logbigram =
                    if (prev == null) calcLogBigram(it, null)
                    else calcLogBigram(it, e_melody.prev().mostLikely as Int)
                score[it] = w1 * simil + logbigram
            }
            e_melody.setEvidence(argmax(score))
        }
    }

    fun calcLogBigram(nn: Int, prev: Int?): Double {
        // println(nn + " " + prev)
        return if (prev == null) Math.log(unigram1[nn] * 0.95 + 0.05)
        else Math.log(bigram[prev][nn] * 0.95 + 0.05)
    }

    fun argmax(list: List<Double>): Int {
        var max = list[0]
        var index = 0
        list.forEachIndexed { i, x ->
            if (x > max!!) {
                max = x
                index = i
            }
        }
        return index
    }

}
