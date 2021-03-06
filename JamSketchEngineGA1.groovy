import org.apache.commons.math3.genetics.*
import jp.crestmuse.cmx.inference.*
import jp.crestmuse.cmx.inference.models.*
import static java.lang.Double.*

class JamSketchEngineGA1 extends JamSketchEngineAbstract {

  int lastUpdateMeasure = -1
  long lastUpdateTime = -1

  double RHYTHM_THRESHOLD = 0.08
//  double RHYTHM_THRESHOLD = 0.25
  def RHYTHMS = [[1, 0, 0, 0, 0, 0], [1, 0, 0, 1, 0, 0],
		 [1, 0, 0, 1, 0, 1], [1, 0, 1, 1, 0, 1],
		 [1, 0, 1, 1, 1, 1], [1, 1, 1, 1, 1, 1]];

  Map<String,Double> parameters() {
    [W1: 1.5, W2: 2.0, W3: 3.0, W4: 20.0, ENT_BIAS: cfg.ENT_BIAS]
  }

  Map<String,String> paramDesc() {
    [W1: "fitness to drawing", W2: "fitness to style",
     ENT_BIAS: "melodic complexity"]
  }
  
  def musicCalculatorForOutline() {
    def hmm = new HMMContWithGAImpl
    (new JamSketchEngineGACalc1(model, cfg, this),
     (int)(cfg.GA_POPUL_SIZE / 2), cfg.GA_POPUL_SIZE, (double)0.2,
     new UniformCrossover(0.5), (double)0.8, new BinaryMutation(),
     (double)0.2, new TournamentSelection(10))
    def calc = new MostSimpleHMMContCalculator(OUTLINE_LAYER, MELODY_LAYER,
					   hmm, mr)
    calc.setCalcLength(cfg.CALC_LENGTH * cfg.DIVISION)
    calc.enableSeparateThread(true)
    calc
  }

  def outlineUpdated(measure, tick) {
    long currentTime = System.nanoTime()
    if (tick == cfg.DIVISION - 1 &&
        lastUpdateMeasure != measure &&
	currentTime - lastUpdateTime >= 100000) {
      applyRhythm(measure, decideRhythm(measure, RHYTHM_THRESHOLD))
      mr.getMusicElement(OUTLINE_LAYER, measure, tick).resumeUpdate()
      lastUpdateMeasure = measure
      lastUpdateTime = currentTime
    }
  }

  def automaticUpdate() {
    false
  }

  def applyRhythm(measure, rhythm) {
    println(rhythm)
    rhythm.eachWithIndex{ r, i ->
      def e = mr.getMusicElement(MELODY_LAYER, measure, i)
      if (measure == 0 && i == 0) {
        e.setRest(r == 0)
	e.setTiedFromPrevious(false)
      } else {
        e.setTiedFromPrevious(r == 0)
      }
    }
  }
  
  def decideRhythm(measure, thresh) {
    def r1 = [1]
    (0..<(cfg.DIVISION-1)).each { i ->
      def curve0 = getMelodicOutline(measure, i)
      def curve1 = getMelodicOutline(measure, i+1)
      if (isNaN(curve1)) {
	r1.add(0)
      } else if (isNaN(curve0) && !isNaN(curve1)) {
	r1.add(1)
      } else if (Math.abs(curve1 - curve0) >= thresh) {
	r1.add(1)
      } else {
	r1.add(0)
      }
    }
    def r1div = []
    def len = RHYTHMS[0].size()
    for (int i = 1; i * len <= r1.size(); i++) {
      r1div.add(r1.take(i * len).takeRight(len))
    }
    def r2 = []
    r1div.each { r1sub ->
      def diff = RHYTHMS.collect { r -> abs(sub(r, r1sub)).sum()}
      r2.add(RHYTHMS[argmin(diff)])
    }
    r2.sum()
  }

  def sub(x, y) {
    def z = []
    x.indices.each{ i ->
      if (x[i] != null && y[i] != null)
	z.add(x[i] - y[i])
    }
    z
  }
    
  def abs(x) {
    x.collect{ Math.abs(it) }
  }
    
  def argmin(x) {
    def minvalue = x[0]
    def argmin = 0
    x.indices.each { i ->
      if (x[i] < minvalue) {
	minvalue = x[i]
	argmin = i
      }
    }
    argmin
  }


  
}
