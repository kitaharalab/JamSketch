import jp.crestmuse.cmx.filewrappers.*
import jp.crestmuse.cmx.elements.*
import dk.ange.octave.*
import dk.ange.cotave.type.*
import jp.crestmuse.cmx.math.*
import static jp.crestmuse.cmx.math.MathUtils.*
import static jp.crestmuse.cmx.math.Operations.*
import static java.lang.Math.*
import groovy.transform.*
//import static Config.*
import static JamSketch.CFG


class FeatureExtractor {

  static {
    DoubleArray.mixin(Operations)
    DoubleMatrix.mixin(Operations)
  }
  
  def part
  def chordprog
  def octave
  int beatsPerMeasure
  int TICKS_PER_BEAT = 480

  def ATTR_NOMIAL = 
  [keyMode: ["major", "minor"],
   chord_type: ["+", "6", "7", "7#11", "7#5", "7#9", "7b5", "7b9", "Maj7",
		"c", "dim", "dim7", "m", "m6", "m7", "m7b5"],
   isChordN: ["n", "y"],
   mtr: ["s", "ss", "w", "ww"],
   nar1: ["P", "R", "D", "ID", "IP", "VP", "IR", "VR", "SA", "SB", "NA"],
   nar2: ["P", "R", "D", "ID", "IP", "VP", "IR", "VR", "SA", "SB", "NA"],
   nar3: ["P", "R", "D", "ID", "IP", "VP", "IR", "VR", "SA", "SB", "NA"],
   phrase: ["f", "i", "m"]
  ]

  def script = """
%    nmat
    d_mel_atract = melattraction(nmat);
    d_tessitura = tessitura(nmat);
    d_mobility = mobility(nmat);
    d_complex_trans = compltrans(nmat);
    d_complex_expect = complebm(nmat, 'o');
    d_boundary = boundary(nmat);
  """

  def start(part, chordprog, beatsPerMeasure) {
    def octavefactory = new OctaveEngineFactory()
    def osname = System.getProperty("os.name").substring(0, 3).toLowerCase()
    if (CFG.OCTAVE_PROGRAM[osname] != null)
      octavefactory.setOctaveProgram(new File(CFG.OCTAVE_PROGRAM[osname]))
	def uri = getClass().getClassLoader().getResource("MIDItoolbox").toURI()
	octavefactory.setWorkingDir(new File(uri))
    this.part = part
    this.chordprog = chordprog
    this.beatsPerMeasure = beatsPerMeasure
  }

  def stop() {
    octave.close()
  }

  def synchronized extractFeatureMapSeq(int from, int thru, int ticksPerBeat) {
    def notelist =
      SCCUtils.getNotesBetween(part, from, thru, ticksPerBeat, true, true)
    if (notelist.size() < 3)
      return [notelist, null]
    part.calcMilliSec()
    def nmat = SCCUtils.toMatrix(notelist, part.channel())
    //    println nmat
    octave.eval("clear")
    octave.put("nmat", toOctave(nmat))
    octave.eval(script)
    def data = [:]
    ["mel_atract", "tessitura", "mobility",
     "complex_trans", "complex_expect", "boundary"].each {
      data[it] = fromOctave(octave, "d_"+it)
    }
    data.meanBoundary = data.boundary.getColumn(0).mean()
    def features = []
    for (i in 0..<notelist.size()) {
      features.add(extractAllFeatures(i, notelist, data))
    }
    [notelist, features]
  }

  @CompileStatic
  long onset(MutableNote note) {
    if (!note.hasAttribute("OriginalOnset")) {
      long onset = note.onset(TICKS_PER_BEAT)
      note.setAttribute("OriginalOnset", onset)
      onset
    } else {
      note.getAttributeLong("OriginalOnset")
    }
  }

  @CompileStatic
  double onsetInMilliSec(MutableNote note) {
    if (!note.hasAttribute("OriginalOnsetInMilliSec")) {
      double onset = note.onsetInMilliSec()
      note.setAttribute("OriginalOnsetInMilliSec", onset)
      onset
    } else {
      note.getAttributeDouble("OriginalOnsetInMilliSec")
    }
  }

  @CompileStatic
  long duration(MutableNote note) {
    if (!note.hasAttribute("OriginalDuration")) {
      long duration = note.duration(TICKS_PER_BEAT)
      note.setAttribute("OriginalDuration", duration)
      duration
    } else {
      note.getAttributeLong("OriginalDuration")
    }
  }

  @CompileStatic
  double durationInMilliSec(MutableNote note) {
    if (!note.hasAttribute("OriginalDurationInMilliSec")) {
      double duration = note.durationInMilliSec()
      note.setAttribute("OriginalDurationInMilliSec", duration)
      duration
    } else {
      note.getAttributeDouble("OriginalDurationInMilliSec")
    }
  }
  
  def extractAllFeatures(i, notelist, data) {
    def note = notelist[i]
    def prev = i < 1 ? null : notelist[i-1]
    def prev2 = i < 2 ? null : notelist[i-2]
    def next = i >= notelist.size()-1 ? null : notelist[i+1]
    def features =
      [
	onset_b:    onset(note) / TICKS_PER_BEAT, 
       duration_b:  duration(note) / TICKS_PER_BEAT,
       pitch:       note.notenum(),
       onset_s:     onsetInMilliSec(note) / 1000, 
       dur_s :      durationInMilliSec(note) / 1000,
       pre_dur_b:   prev == null ? 0.0 : duration(prev) / TICKS_PER_BEAT,
       pre_dur_s:   prev == null ? 0.0 : durationInMilliSec(prev) / 1000,
       nxt_dur_b:   next == null ? 0.0 : duration(next) / TICKS_PER_BEAT,
       nxt_dur_s:   next == null ? 0.0 : durationInMilliSec(next) / 1000,
       onset_b_mod: (onset(note) / TICKS_PER_BEAT).remainder(beatsPerMeasure), 
       pitch_mod:   note.notenum() % 12, 
       prev_int:    (prev == null ? notelist.last().notenum() - note.notenum() :
		     prev.notenum() - note.notenum()), 
       next_int:    (next == null ?
		     notelist.first().notenum() - note.notenum() :
		     next.notenum() - note.notenum()), 
       keyFifhts:   0,   /* "C" is assumed */
       keyMode:     "major",  
       note2key:    note.notenum() % 12 - 0,
       chord_id:    getChordAt(note).root().number(),
       chord_type:  "7",  /* seventh chords are assumed */
       note2chord:  ((note.notenum()%12)-getChordAt(note).root().number()+12)%12,
       isChordN:    (getChordAt(note).notes().any{
		       it.number() == (note.notenum() % 12)
		     } ? 'y' : 'n'),
       mtr:         mtr(note), 
       nar_rd:      prev2 == null ? 0.0 : narmour_rd(prev2, prev, note), 
       nar_rr:      prev2 == null ? 0.0 : narmour_rd(prev2, prev, note), 
       nar_id:      prev2 == null ? 0.0 : narmour_id(prev2, prev, note), 
       nar_cl:      prev2 == null ? 0.0 : narmour_cl(prev2, prev, note), 
       nar_pr:      prev == null ? 0.0 : narmour_pr(prev, note),
       nar_co:      prev2 == null ? 0.0 : narmour_cl(prev2, prev, note),
       ton_stab:    tonality(note),
       mel_atract:  data.mel_atract[i], 
       tessitura:   data.tessitura[i],
       mobility:    data.mobility[i],
       nar1:        i < notelist.size()-2 ? narmour_sig(notelist[i],
							notelist[i+1],
							notelist[i+2]) : 'NA', 
       nar2:       i < notelist.size()-3 ? narmour_sig(notelist[i+1],
						       notelist[i+2],
						       notelist[i+3]) : 'NA', 
       nar3:       i < notelist.size()-4 ? narmour_sig(notelist[i+2],
						       notelist[i+3],
						       notelist[i+4]) : 'NA', 
       complex_trans: data.complex_trans[0],
       complex_expect: data.complex_expect[0],
       tempo:      120,   // tempo=120 is assumed
       phrase:     (i >= notelist.size()-1 ? 'f' :
		    phrase(data.boundary[i], data.boundary[i+1],
			   data.meanBoundary))
      ]
  }

  def getChordAt(note) {
    int measure = (onset(note) / TICKS_PER_BEAT / beatsPerMeasure)
    //    chordprog[(measure - 2) % chordprog.size()]
    chordprog[measure]
  }

  @CompileStatic
  String mtr(MutableNote note) { /* metrical strength */
    long onset = onset(note)
    if (onset % TICKS_PER_BEAT != 0) {
      "ww"
    } else if (onset % (TICKS_PER_BEAT * beatsPerMeasure) == 0) {
      "ss"
    } else if (onset % ((int)(TICKS_PER_BEAT * beatsPerMeasure / 2)) == 0) {
      "s"
    } else {
      "w"
    }
  }

  @CompileStatic
  int narmour_rd(MutableNote prev2, MutableNote prev, MutableNote note) {
    int interval1 = prev.notenum() - prev2.notenum()
    int interval2 = note.notenum() - prev.notenum()
    if (interval1 < 6) {
      0
    } else if (interval1 > 6 && sign(interval1) != sign(interval2)) {
      1
    } else if (interval1 > 6 && sign(interval1) == sign(interval2)) {
      -1
    } else {
      0
    }
  }

  @CompileStatic
  int narmour_cl(MutableNote prev2, MutableNote prev, MutableNote note) {
    int interval1 = prev.notenum() - prev2.notenum()
    int interval2 = note.notenum() - prev.notenum()
    if (sign(interval1) != sign(interval2) &&
	abs(interval1) - abs(interval2) < 3) {
      1
    } else if (sign(interval1) != sign(interval2) &&
	       abs(interval1) - abs(interval2) > 2) {
      2
    } else if (sign(interval1) == sign(interval2) &&
	       abs(interval1) - abs(interval2) > 3) {
      1
    } else {
      0
    }
  }

  @CompileStatic
  int narmour_id(MutableNote prev2, MutableNote prev, MutableNote note) {
    int interval1 = prev.notenum() - prev2.notenum()
    int interval2 = note.notenum() - prev.notenum()
    if (interval1 < 6 && sign(interval1) != sign(interval2) &&
	abs(abs(interval1)- abs(interval2)) < 3) {
      1
    } else if (interval1 < 6 && sign(interval1) == sign(interval2) &&
	       abs(abs(interval1) - abs(interval2)) < 4) {
      1
    } else if (interval1 > 6 &&
	       abs(interval1) >= abs(interval2)) {
      1
    } else {
      0
    }
  }

  @CompileStatic
  int narmour_pr(MutableNote prev, MutableNote note) {
    abs(note.notenum() - prev.notenum())
  }

  @CompileStatic
  double narmour_co(MutableNote prev2, MutableNote prev, MutableNote note) {
    double[] consonance = [10, 1, 4, 16, 6.03, 7.25, 8.03, 5.76, 9.16, 6.32,
			  7.76, 5.66, 3.8]
    int pcdiff = note.notenum() - prev.notenum()
    int iv12 = (abs(pcdiff) % 12)
    consonance[iv12]
  }

  @CompileStatic
  double tonality(MutableNote note) {  /* C-major key is assumed */
    double[] ref=[6.35,2.23,3.48,2.33,4.38,4.09,2.52,5.19,2.39,3.66,2.29,2.88]
    ref[note.notenum() % 12]
  }

  @CompileStatic
  String narmour_sig(MutableNote note1, MutableNote note2, MutableNote note3) {
    int d1 = note2.notenum() - note1.notenum()
    int d2 = note3.notenum() - note2.notenum()
    if (intsize2(d1, d2) == 'ss' && samedir(d1, d2)) {
      'P'
    } else if (intsize2(d1, d2) == 'ls' && !samedir(d1, d2)) {
      'R'
    } else if (d1 == 0 && d2 == 0) {
      'D'
    } else if (d1 == d2 && d1 < 6) {
      'ID'
    } else if (intsize2(d1, d2) == 'ss' && !samedir(d1, d2)) {
      'IP'
    } else if (intsize2(d1, d2) == 'sl' && samedir(d1, d2)) {
      'VP'
    } else if (intsize2(d1, d2) == 'ls' && samedir(d1, d2)) {
      'IR'
    } else if (intsize2(d1, d2) == 'll' && !samedir(d1, d2)) {
      'VR'
    } else if (intsize2(d1, d2) == 'll' && samedir(d1, d2)) {
      'SA'
    } else if (intsize2(d1, d2) == 'sl' && !samedir(d1, d2)) {
      'SB'
    } else {
      'NA'
    }
  }

  @CompileStatic
  String intsize2(int d1, int d2) {
    if (abs(d1) < 6 && abs(d2) < 6) {
      'ss'
    } else if (abs(d1) < 6 && abs(d2) >= 6) {
      'sl'
    } else if (abs(d1) >= 6 && abs(d2) < 6) {
      'ls'
    } else {
      'll'
    }
  }

  @CompileStatic
  boolean samedir(int d1, int d2) {
    d1 * d2 > 0
  }

  @CompileStatic
  String phrase(double bound, double next, double mean) {
    if (bound >= 2 * mean) {
      'i'
    } else if (next >= 2 * mean) {
      'f'
    } else {
      'm'
    }
  }




}
