class JamSketchEngineGA2 extends JamSketchEngineGA1 {
  def rmodel = null

  def parameters() {
    super().parameters() + [RHYTHM_DENSITY: cfg.RHYTHM_DENSITY]
  }
  
  def decideRhythm(measure, thresh) {
    if (rmodel == null)
      rmodel = new RhythmModel(this, cfg)
    def curve = [null] * cfg.DIVISION
    (0..<cfg.DIVISION).each { i ->
      def value = getMelodicOutline(measure, i)
      if (!Double.isNaN(value)) {
	curve[i] = value
      }
    }
    rmodel.decideRhythm(curve)
  }
}
