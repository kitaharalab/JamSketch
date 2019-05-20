import groovy.transform.*
import groovy.json.*
import org.apache.commons.math3.genetics.*
import jp.crestmuse.cmx.inference.*
import jp.crestmuse.cmx.inference.models.*
import java.util.concurrent.*
import static JamSketch.CFG


class RhythmModel {

  class RhythmTree {
    int note, freq
    List<RhythmTree> next = []
    }

  RhythmTree rhythmtree
  HMM ga
  int DIVISION
  int GA_TIME
  static double R_TH = 0.25
  static double RHYTHM_DENSITY
  
  RhythmModel() {
    def json = new JsonSlurper()
	def uri = getClass().getClassLoader().getResource(CFG.MODEL_FILE).toURI()
    def model = json.parseText((new File(uri)).text)
    rhythmtree = makeRhythmTree(model.rhythmtree)
    ga = new HMMContWithGAImpl(new RhythmGACalc(),
			       (int)(CFG.GA_POPUL_SIZE / 2),
			       CFG.GA_POPUL_SIZE, (double)0.2,
			       new UniformCrossover(0.5), (double)0.8,
			       new BinaryMutation(), (double)0.2,
			       new TournamentSelection(10))
    DIVISION = CFG.DIVISION
    GA_TIME = CFG.GA_TIME_R
    RHYTHM_DENSITY = CFG.RHYTHM_DENSITY
  }

  def makeRhythmTree(map) {
    if (map != null) {
      def node = new RhythmTree(note: map.note, freq: map.freq)
      map.next.each {
	node.next.add(makeRhythmTree(it))
      }
      node
    } else {
      null
    }
  }

  def decideRhythm(curve) {
    ga.mostLikelyStateSequence(curve, null)
  }

  def decideRhythm(curve, double density) {
    ga.mostLikelyStateSequence(curve, null, new RhythmGACalc(density))
  }
  
  class RhythmGACalc extends GACalculator<Integer,Double> {

    double rhythmDensity

    RhythmGACalc() {
      this(RhythmModel.RHYTHM_DENSITY)
    }
    
    RhythmGACalc(double density) {
      rhythmDensity = density;
    }
    
    @CompileStatic
    StoppingCondition getStoppingCondition() {
      new FixedElapsedTime(GA_TIME, TimeUnit.MILLISECONDS);
    }

    @CompileStatic
    void populationUpdated(Population p, int gen, List<MusicElement> e) {
      Chromosome c = p.getFittestChromosome()
    }

    @CompileStatic
    List<Integer> createInitial(int size) {
      List<Integer> seq = [0] * DIVISION
      generateRhythm(rhythmtree, seq, 0)
      seq
    }

    @CompileStatic
    double calcFitness(List<Integer> s, List<Double> o, List<MusicElement> e) {
      double rmse = calcRMSE(s, o);
      double likelihood = evaluateRhythm(rhythmtree, s, 0)
      double density = calcRhythmDensity(s)
      //      println("RMSE=${rmse}\tL=${likelihood}")
      //1.0 * rmse + 0.1 * likelihood + 0.0 * density
      //1.0 * rmse + 1.0 * likelihood + 0.0 * density
      //1.0 * rmse + 1.0 * likelihood + 1.0 * density      //í≤êﬂ
	  0.1 * rmse + 0.1 * likelihood + 1.2 * density
    }

    @CompileStatic
    List<Integer> makeTentativeRhythm(List<Double> curve) {
      List<Integer> r1 = [1]
      double th = R_TH
      (0..<(curve.size()-1)).each { i ->
	if (curve[i+1] == null) {
	  r1.add(0)
	} else if (curve[i] == null && curve[i+1] != null) {
	  r1.add(1)
	} else {
	  double d = Math.abs(curve[i+1] - curve[i])
	  double p = Math.tanh(2.0 * d)
	  //	  System.out.println(p)
	  r1.add(Math.random() < p ? 1 : 0)
	  //	} else if (Math.abs(curve[i+1] - curve[i]) >= th) {
	  //	  r1.add(1)
	  //	  	} else if (Math.abs(curve[i+1] - curve[i]) >= th / 2) {
	  //	  	  r1.add(Math.random() >= 0.5 ? 1 : 0)
	  //	} else {
	  //	  r1.add(0)
	}
      }
      //      println(r1)
      r1
    }

    @CompileStatic
    double calcRMSE(List<Integer> s, List<Double> o) {
      List<Integer> r1 = makeTentativeRhythm(o)
      double e = 0.0
      for (int i = 0; i < s.size(); i++) {
	e += (r1[i] - s[i]) * (r1[i] - s[i])
      }
      -Math.sqrt(e)
    }

    @CompileStatic
    double calcRhythmDensity(List<Integer> s) {
      double e = (rhythmDensity - (int)s.sum())
      -e * e
    }
    
    @CompileStatic
    void generateRhythm(RhythmTree tree, List<Integer> seq, int index) {
      if (tree.next[0] != null && tree.next[1] != null) {
	double freq0 = tree.next[0].freq
	double freq1 = tree.next[1].freq
	if (Math.random() < freq0 / (freq0 + freq1)) {
	  seq[index] = 0
	  generateRhythm(tree.next[0], seq, index+1)
	} else {
	  seq[index] = 1
	  generateRhythm(tree.next[1], seq, index+1)
	}
      } else if (tree.next[0] != null && tree.next[1] == null) {
	seq[index] = 0
	generateRhythm(tree.next[0], seq, index+1)
      } else if (tree.next[0] == null && tree.next[1] != null) {
	seq[index] = 1
	generateRhythm(tree.next[1], seq, index+1)
      }
    }

    @CompileStatic
    double evaluateRhythm(RhythmTree tree, List<Integer> seq, int index) {
      if (index < seq.size()) {
	if (tree.next[0] == null && tree.next[1] == null) {
	  return Double.NEGATIVE_INFINITY;
	} else if (tree.next[0] == null && tree.next[1] != null) {
	  if (seq[index] == 0) {
	    return Double.NEGATIVE_INFINITY;
	    //return Math.log(0.001)
	  } else {
	    return evaluateRhythm(tree.next[1], seq, index+1)
	  }
	} else if (tree.next[0] != null && tree.next[1] == null) {
	  if (seq[index] == 0) {
	    return evaluateRhythm(tree.next[0], seq, index+1)
	  } else {
	    return Double.NEGATIVE_INFINITY;
	    //return Math.log(0.001)
	  }
	} else {
	  double freq0 = tree.next[0].freq
	  double freq1 = tree.next[1].freq
	  if (seq[index] == 0) {
	    double logL = Math.log(freq0 / (freq0 + freq1))
	    return logL + evaluateRhythm(tree.next[0], seq, index+1)
	  } else {
	    double logL = Math.log(freq1 / (freq0 + freq1))
	    return logL + evaluateRhythm(tree.next[1], seq, index+1)
	  }
	}
      } else {
	0.0
      }
    }
  }
  
}

