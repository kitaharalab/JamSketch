package jp.kthrlab.jamsketch;

import jp.crestmuse.cmx.filewrappers.SCC;
import jp.crestmuse.cmx.misc.ChordSymbol2;

interface JamSketchEngine {

  fun init(scc: SCC, target_part: SCC.Part, cfg: Config);
  fun setMelodicOutline(measure: Int, tick: Int, value: Double?);
  fun getChord(measure: Int, tick: Int): ChordSymbol2;
  fun setFirstMeasure(number: Int);
  fun resetMelodicOutline();
  fun parameters(): Map<String, Double>;
  fun paramDesc(): Map<String, String>;
}
