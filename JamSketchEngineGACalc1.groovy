import groovy.transform.*
import jp.crestmuse.cmx.inference.*
import jp.crestmuse.cmx.inference.models.*
import org.apache.commons.math3.genetics.*
import java.util.concurrent.*

class JamSketchEngineGACalc1 implements GACalculator<Integer,Double> {
//class JamSketchEngineGACalc1 extends GACalculator<Integer,Double> {

  class MelodyTree {
    int note,freq
    List<MelodyTree> next = []
    }
  
  //  double W1 = 1.0, W2 = 1.0
  Map<String,List<Double>> trigram
  double[][] bigram
  double[][] delta_bigram
  Map<String,List<Double>> chord_beat_dur_unigram
  double entropy_mean
  MelodyTree melodytree
  int GA_TIME, GA_POPUL_SIZE
  String GA_INIT
  int BEATS_PER_MEASURE, DIVISION
  //  double ENT_BIAS
  JamSketchEngineGA1 engine

  List<List<List<Integer>>> favorites = []
  
  JamSketchEngineGACalc1(model, CFG, engine) {
    trigram = model.trigram
    bigram = model.bigram
    delta_bigram = model.delta_bigram
    chord_beat_dur_unigram = model.chord_beat_dur_unigram
    entropy_mean = model.entropy.mean
    melodytree = makeMelodyTree(model.melodytree)
    GA_TIME = CFG.GA_TIME
    GA_POPUL_SIZE = CFG.GA_POPUL_SIZE
    GA_INIT = CFG.GA_INIT
    BEATS_PER_MEASURE = CFG.BEATS_PER_MEASURE
    DIVISION = CFG.DIVISION
    //    ENT_BIAS = CFG.ENT_BIAS
    this.engine = engine
  }

  def makeMelodyTree(map) {
    if (map != null) {
      def node = new MelodyTree(note: map.note, freq: map.freq)
      map.next.each {
	node.next.add(makeMelodyTree(it))
      }
      node
    } else {
      null
    }
  }


    @CompileStatic
    StoppingCondition getStoppingCondition() {
      //new FixedGenerationCount(5)
      new FixedElapsedTime(GA_TIME, TimeUnit.MILLISECONDS);
    }
    
    @CompileStatic
    void populationUpdated(Population p, int gen, List<MusicElement> e) {
      Chromosome c = p.getFittestChromosome()
      //      println("Population,${e[0].measure()},${e[0].tick()},${gen}," +
      //      	      "${c.getFitness()},\"${c}\"")
      //      //      println(p.getFittestChromosome().getFitness());
    }
    
    Random random = new Random()

    @CompileStatic
    List<Integer> createInitial(int size) {
      if (favorites[size] != null) {
	favorites[size][random.nextInt(favorites[size].size())]
      } else {
	if (GA_INIT.equalsIgnoreCase("random"))
	  createInitialRandom(size)
	  else if (GA_INIT.equalsIgnoreCase("tree"))
        createInitialFromTree(size)
	else
	  throw new IllegalArgumentException("GA_INIT \"${GA_INIT}\" is not supported")
      }
    }
    
    @CompileStatic
    List<Integer> createInitialRandom(int size) {
      List<Integer> seq = []
      for (int i in 0..<size) {
	seq.add((12 * Math.random()) as int)
      }
      seq
    }

    @CompileStatic
    List<Integer> createInitialFromTree(int size) {
      List<Integer> seq= []
      while (seq.size() < size) {
	chooseNoteFromTree(melodytree, seq, size)
      }
      seq
    }

    @CompileStatic
    void chooseNoteFromTree(MelodyTree tree, List<Integer> seq, int size) {
      if (seq.size() < size && tree != null && tree.next.size() > 0) {
	int rand = (Math.random() * tree.next.size()) as int
	seq.add(tree.next[rand].note)
	chooseNoteFromTree(tree.next[rand], seq, size)
      }
    }
    
    @CompileStatic
    double calcFitness(List<Integer> s, List<Double> o, List<MusicElement> e) {
      double W1 = engine.parameters().W1
      double W2 = engine.parameters().W2
      double W3 = engine.parameters().W3
      double W4 = engine.parameters().W4
      double ENT_BIAS = engine.parameters().ENT_BIAS
      double mse = -calcRMSE(s, o)
      double lik = calcLogTrigramLikelihood(s)
      //double lik = calcLogTransLikelihood(s)
      //      double delta = calcLogDeltaTransLikelihood(s)
      double entr = calcEntropy(s, 12)
      double entrdiff =
	-(entr-entropy_mean-ENT_BIAS) * (entr-entropy_mean-ENT_BIAS)
      double chord = calcLogChordBeatUnigramLikelihood(s, e)
      W1 * mse + W2 * lik + W3 * chord + W4 * entrdiff
      //3 * mse + 2 * lik + 0 * delta + 3 * chord + 20 * entrdiff
      //3 * mse + 2 * lik + 1 * delta + 3 * chord + 20 * entrdiff
      //      3 * mse + 2 * lik + delta + 3 * chord + 10 * entrdiff
    }

    @CompileStatic
    double calcRMSE(List<Integer> s, List<Double> o) {
      double e = 0
      int length = s.size()
      for (int i = 0; i < length; i++) {
	double e1 = (o[i] % 12) - (s[i] % 12)
	e += e1 * e1;
      }
      Math.sqrt(e)
    }

    @CompileStatic
    double calcLogTrigramLikelihood(List<Integer> s) {
      double lik = 0.0
      int length = s.size() - 2;
      for (int i = 0; i < length; i++) {
	String key = s[i] + "," + s[i+1]
	lik += Math.log((trigram.containsKey(key) ? trigram[key][s[i+2]] : 0.001) as double)
      }
      lik
    }
    
    @CompileStatic
    double calcLogTransLikelihood(List<Integer> s) {
      double lik = 0.0
      int length = s.size() - 1;
      for (int i = 0; i < length; i++) {
	lik += Math.log(bigram[s[i]][s[i+1]] as double)
      }
      lik
    }

    @CompileStatic
    double calcLogDeltaTransLikelihood(List<Integer> s) {
      double lik = 0.0
      int length = s.size() - 2
      for (int i = 0; i < length; i++) {
	lik += Math.log(delta_bigram[s[i+1]-s[i]][s[i+2]-s[i+1]] as double)
      }
      lik
    }

    @CompileStatic
    double calcLogChordBeatUnigramLikelihood(List<Integer> s,
					     List<MusicElement> e) {
      double lik = 0.0
      int length = s.size() - 1
      for (int i = 0; i < length; i++) {
	String chord = engine.getChord(i/DIVISION as int, 0).toString()
	//String chord = chordprog[i/DIVISION as int].toString()
	int div4 = DIVISION / BEATS_PER_MEASURE as int
	String beat = (e[i].tick() == 0 ? "head" :
		       (e[i].tick() % div4 == 0 ? "on" : "off"))
	//String dur = (e[i].duration() >= div4 ? "mid" : "short")
	String dur =
	  (e[i].duration() >= 2 * div4 ? "long" :
	   (e[i].duration() >= div4 ? "mid" : "short"))
	  String key = chord + "_" + beat + "_" + dur
	  //String key = chord + "_" + beat
	//String key = chord
	  lik += Math.log(((chord_beat_dur_unigram[key][s[i]] ?: 0) + 0.001) as double)
      }
      lik
    }

    @CompileStatic
    double calcEntropy(List<Integer> s, int maxvalue) {
      int[] freq = [0] * maxvalue
      int sum = 0
      for (int i = 0; i < s.size(); i++) {
	freq[s[i]] += 1
        sum++
	  }
      double entropy = 0.0
      for (int i = 0; i < maxvalue; i++) {
	if (freq[i] > 0) {
	  double p = (double)freq[i] / sum
	  entropy += -Math.log(p) * p / Math.log(2)
	}
      }
      entropy
    }

}
