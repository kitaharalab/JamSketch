class MelodyData2 {
  def width
  def pianoroll
  def engine
  def curve1
  def scc
  def cfg
  
  MelodyData2(filename, width, cmxcontrol, pianoroll, cfg) {
    this.width = width
    this.pianoroll = pianoroll
    this.cfg = cfg
    scc = cmxcontrol.readSMFAsSCC(filename)
    scc.repeat(cfg.INITIAL_BLANK_MEASURES * cfg.BEATS_PER_MEASURE *
	       scc.division,
	       (cfg.INITIAL_BLANK_MEASURES + cfg.NUM_OF_MEASURES) *
	       cfg.BEATS_PER_MEASURE * scc.division, cfg.REPEAT_TIMES - 1)
    def target_part = scc.getFirstPartWithChannel(1)
    engine = new JamSketchEngineGA1()
//    engine = Class.forName(cfg.JAMSKETCH_ENGINE).newInstance()
    engine.init(scc, target_part, cfg)
    resetCurve()
  }

  def resetCurve() {
    curve1 = [null] * width
    engine.resetMelodicOutline()
  }

  def updateCurve(int from, int thru) {
    int nMeas = cfg.NUM_OF_MEASURES
    int div = cfg.DIVISION
    int size2 = nMeas * div
    for (int i in from..thru) {
      double nn = (curve1[i] == null ? null : pianoroll.y2notenum(curve1[i]))
      int ii = i - 100
      int position = (int)(ii * size2 / (curve1.size() - 100))
      engine.setMelodicOutline((int)(position / div), position % div, nn)
    }
  }
}	    
