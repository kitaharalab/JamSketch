package jp.kthrlab.jamsketch

//import groovy.transform.*
import android.os.Handler
import android.os.HandlerThread
import com.beust.klaxon.JsonObject
import com.beust.klaxon.lookup
import jp.crestmuse.cmx.inference.MusicCalculator
import jp.crestmuse.cmx.inference.MusicElement
import jp.crestmuse.cmx.inference.MusicRepresentation
import jp.crestmuse.cmx.misc.ChordSymbol2

class NoteSeqGenerator(
        private var noteLayer: String,
        private var chordLayer: String,
        private var beatsPerMeas: Int,
        private var entropy_bias: Double,
        private val model: Any
) : MusicCalculator {

  var trigram: Map<String,List<Double>>? = null
  var bigram: List<List<Double>>? = null
  var chord_beat_dur_unigram: Map<String,List<Double>>? = null
  var entropy_mean: Double? = null
  var w1 = 0.5
  var w2 = 0.8
  var w3 = 1.2
  var w4 = 2.0
  var RHYTHM_THRS = 0.1
  var RHYTHM_WEIGHTS = listOf(1.0, 0.2, 0.4, 0.8, 0.2, 0.4,
          1.0, 0.2, 0.4, 0.8, 0.2, 0.4)
  var handlerThread = HandlerThread("noteSeqGenerator_thread")
  var handler:Handler? = null

  init {
    if (model is JsonObject) {
      trigram = model.map["trigram"] as Map<String, List<Double>>
      bigram = model.map["bigram"] as List<List<Double>>
      chord_beat_dur_unigram = model.map["chord_beat_dur_unigram"] as Map<String, List<Double>>
      entropy_mean = model.lookup<Double>("entropy.mean")[0]
    }
    handlerThread.start()
    handler = Handler((handlerThread.getLooper()))
  }

//  @CompileStatic
  fun prev(e: MusicElement?, rep: Int, ifnull: Any): Any {
    if (e == null) {
      return ifnull
    } else if (rep == 0) {
      return e.mostLikely
    } else {
      return prev(e.prev(), rep-1, ifnull)
    }
  }

//  @CompileStatic
  override fun updated(measure: Int, tick: Int, layer: String, mr: MusicRepresentation) {
    var e_curve = mr.getMusicElement(layer, measure, tick)
    var value = e_curve.getMostLikely() as Double
    if (!value.isNaN()) {

      val th = Thread(Runnable {
        var e_melo = mr.getMusicElement(noteLayer, measure, tick)
        var b = decideRhythm(value,
                prev(e_curve, 1, Double.NaN) as Double,
                tick, e_melo, mr)
        if (!b) {
          var prev1 = prev(e_melo, 1, -1)
          var prev2 = prev(e_melo, 2, -1)
          var c = mr.getMusicElement(chordLayer, measure, tick).
          getMostLikely() as ChordSymbol2
          var scores: MutableList<Double?> = MutableList(12){null}
          var prevlist = mutableListOf<Int>()

          for (i in 0 until tick) {
            prevlist.add(
                    mr.getMusicElement(noteLayer, measure, i).getMostLikely() as Int)
          }
          (0..11).forEach { i ->
            var value12 = value - (value / 12).toInt() * 12
            var simil = -Math.log((value12 - i) * (value12 - i))
            var logtrigram = calcLogTrigram(i, prev1 as Int, prev2 as Int)
            var logchord = calcLogChordBeatUnigram(i, c, tick, beatsPerMeas,
                    e_melo.duration(), mr)
            var entdiff = calcEntropyDiff(i, prevlist)
            scores[i] = w1 * simil + w2 * logtrigram + w3 * logchord +
                    w4 * (-entdiff)
          }
          e_melo.setEvidence(argmax(scores))
        }
      })
      handler?.post(th)
//      th.start()
      System.err.println("_________________thread started")
    }
  }

  fun decideRhythm(value: Double, prev: Double, tick: Int, e: MusicElement, mr: MusicRepresentation): Boolean {
    e.setTiedFromPrevious(false)
    if (!value.isNaN() && !prev.isNaN()) {
      var score = Math.abs(value - prev) * RHYTHM_WEIGHTS[tick]
      if (score < RHYTHM_THRS) {
	e.setTiedFromPrevious(true)
        return true
      }
    }
      return false
  }

//  @CompileStatic
  fun calcLogTrigram(number: Int, prev1: Int, prev2: Int): Double {
    if (prev2 == -1) {
      if (prev1 == -1) {
        return 0.0
      } else {
        println("      " + prev1)
        println("      " + number)
        println(bigram)
        println(bigram!![prev1][number])
        return Math.log(bigram!![prev1][number])
      }
    } else {
    var key =  "${prev2},${prev1}"
    if (trigram!!.containsKey(key)) {
      return Math.log(trigram!![key]!![number])
    } else {
      return Math.log(0.001)
    }
    }
  }
  
//  @CompileStatic
  fun calcLogChordBeatUnigram(number: Int, chord: ChordSymbol2,
                              tick: Int, beatsPerMeas: Int, duration: Int,
                              mr: MusicRepresentation): Double {
    var s_chord = chord.toString()
    var div4 = mr.getDivision() / beatsPerMeas
    var s_beat = if (tick == 0) "head" else
      (if(tick % div4 == 0) "on" else "off")
    var s_dur = if(duration >= 2 * div4) "long" else
      (if(duration >= div4) "mid" else "short")
    var key = s_chord + "_" + s_beat + "_" + s_dur
    return Math.log(chord_beat_dur_unigram!![key]!![number] + 0.001)
  }

//  @CompileStatic
  fun calcEntropyDiff(number: Int, prevlist: List<Int>): Double {
    var entropy: Double = calcEntropy(number, prevlist)
    var entdiff: Double = entropy - entropy_mean!! - entropy_bias
    return entdiff * entdiff
  }
  
//  @CompileStatic
  fun calcEntropy(number: Int, prevlist: List<Int>): Double {
    var freq = MutableList(12){0}
    var sum = 0
    prevlist.forEach {
      freq[it] += 1
      sum++;
    }
    freq[number] += 1
    sum++;
    var entropy = 0.0
    (0..11).forEach { i ->
      if (freq[i] > 0) {
	var p: Double = freq[i].toDouble() / sum
	entropy += -Math.log(p) * p / Math.log(2.0)
      }
    }
    return entropy
  }
  
//  @CompileStatic
  fun argmax(list: MutableList<Double?>): Int {
    var max = list[0]
    var index = 0
    list.forEachIndexed { i, x ->
      if (x != null) {
        if (x > max!!) {
          max = x
          index = i
        }
      }
    }
    return index
  }

}
