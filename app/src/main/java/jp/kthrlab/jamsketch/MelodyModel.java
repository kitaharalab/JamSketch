package jp.kthrlab.jamsketch;

import org.apache.commons.math3.genetics.BinaryMutation;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.FixedElapsedTime;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.StoppingCondition;
import org.apache.commons.math3.genetics.TournamentSelection;
import org.apache.commons.math3.genetics.UniformCrossover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import be.ac.ulg.montefiore.run.jahmm.ObservationReal;
import groovy.json.JsonSlurper;
import jp.crestmuse.cmx.inference.MostSimpleHMMContCalculator;
import jp.crestmuse.cmx.inference.MusicElement;
import jp.crestmuse.cmx.inference.MusicRepresentation;
import jp.crestmuse.cmx.inference.MusicRepresentationImpl;
import jp.crestmuse.cmx.inference.models.GACalculator;
import jp.crestmuse.cmx.misc.ChordSymbol2;
import jp.crestmuse.cmx.processing.CMXApplet;

class MelodyModel {

  class MelodyTree {
    int note,freq;
    List<MelodyTree> next = new ArrayList<MelodyTree>();
    }

  MusicRepresentation mr;
  List<List <BigDecimal>>  bigram;
  List<List <Object>> delta_bigram;
  Map<String,List<BigDecimal>> chord_beat_dur_unigram;
  double entropy_mean;
  MelodyTree melodytree;
  
  int GA_TIME, GA_POPUL_SIZE;
  String GA_INIT;
  int BEATS_PER_MEASURE, DIVISION;
  List<ChordSymbol2> chordprog = new ArrayList<ChordSymbol2>();

  MelodyModel(CMXApplet cmxcontrol, MelodyData.SCCGenerator sccgenerator) {
    mr = cmxcontrol.createMusicRepresentation(Config.NUM_OF_MEASURES, Config.DIVISION);
    mr.addMusicLayerCont("curve");
//    mr.addMusicLayer("melody", (0..11) as int[]);
    mr.addMusicLayer("melody", new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,11});
    JsonSlurper json = new JsonSlurper();
    Map model = (Map) json.parseText(getContents(Config.MODEL_FILE));
    bigram = ((List<List<BigDecimal>>)model.get("bigram"));
    delta_bigram = ((List<List<Object>>)model.get("delta_bigram"));

    chord_beat_dur_unigram = (Map<String, List<BigDecimal>>) model.get("chord_beat_dur_unigram");
    entropy_mean = ((BigDecimal) ((Map)model.get("entropy")).get("mean")).doubleValue();
    melodytree = makeMelodyTree((Map)model.get("melodytree"));
    MyHMMContWithGAImpl hmm = new MyHMMContWithGAImpl(new MyGACalc(),
            Config.GA_POPUL_SIZE / 2,
            Config.GA_POPUL_SIZE,
                    0.2,
				    new UniformCrossover(0.5),
            0.8,
				    new BinaryMutation(),
            0.2,
				    new TournamentSelection(10));
    MostSimpleHMMContCalculator calc = new MostSimpleHMMContCalculator("curve", "melody", hmm, mr);
    calc.setCalcLength(Config.CALC_LENGTH * Config.DIVISION);
    calc.enableSeparateThread(true);
    mr.addMusicCalculator("curve", calc);
    mr.addMusicCalculator("melody", sccgenerator);

    GA_TIME = Config.GA_TIME;
    GA_POPUL_SIZE = Config.GA_POPUL_SIZE;
    GA_INIT = Config.GA_INIT;
    BEATS_PER_MEASURE = Config.BEATS_PER_MEASURE;
    DIVISION = Config.DIVISION;
    chordprog = Config.chordprog;
//    Arrays.asList(Config.chordprog).forEach(s -> chordprog.add(ChordSymbol2.parse(s.toString())));

  }

  private String getContents (String path) {
    try {
      return new BufferedReader(new InputStreamReader(JamSketchActivity.getMyResources().getAssets().open(path))).lines().collect(Collectors.joining());// res.getAssets().open(path). //Files.lines(Paths.get(URI.create(path))).collect(Collectors.joining());
    }
    catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  MelodyTree makeMelodyTree(Map map) {
    if (map != null) {
      MelodyTree node = new MelodyTree();
      node.note = (int) map.get("note");
      node.freq = (int) map.get("freq");
      ((List)map.get("next")).forEach(it -> node.next.add(makeMelodyTree((Map) it)));
//      node.next.add(makeMelodyTree( (Map)map.get("next")));

//      map.next.each {
//	node.next.add(makeMelodyTree(it);
//      }
      return node;
    } else {
      return null;
    }
  }

  void updateMusicRepresentation(int measure) {
    MusicElement e = mr.getMusicElement("curve", measure + 1, -1);
    e.resumeUpdate();
    ((MusicRepresentationImpl)mr).reflectTies("curve", "melody");
    ((MusicRepresentationImpl)mr).reflectRests("curve", "melody");
  }

//  class MyGACalc extends GACalculator<Integer,Double> {
  class MyGACalc implements GACalculator<Integer, Double>
{

  private List<Integer> s;
  private List<ObservationReal> o;
  private List<MusicElement> e;

  public StoppingCondition getStoppingCondition() {
      //new FixedGenerationCount(5);
      return new FixedElapsedTime(GA_TIME, TimeUnit.MILLISECONDS);
    }
    
    public void populationUpdated(Population p, int gen, List<MusicElement> e) {
      Chromosome c = p.getFittestChromosome();
      System.out.println("Population," + e.get(0).measure() + "," + e.get(0).tick() + "," + gen + "," +
      	      c.getFitness() + "," + c);
      //      println(p.getFittestChromosome().getFitness());
    }
    
    public List<Integer> createInitial(int size) {
      if (GA_INIT.equalsIgnoreCase("random")) {
        return createInitialRandom(size);
      } else if (GA_INIT.equalsIgnoreCase("tree")) {
        return createInitialFromTree(size);
      } else {
        throw new IllegalArgumentException("GA_INIT \"${GA_INIT}\" is not supported");
      }
    }

  List<Integer> createInitialRandom(int size) {
      List<Integer> seq = new ArrayList<Integer>();
      for (int i = 0; i < size; i++) {
	seq.add((int) (12 * Math.random()));
      }
      return seq;
    }

    List<Integer> createInitialFromTree(int size) {
      List<Integer> seq = new ArrayList<Integer>();
      while (seq.size() < size) {
    	chooseNoteFromTree(melodytree, seq, size);
      }
      return seq;
    }

    void chooseNoteFromTree(MelodyTree tree, List<Integer> seq, int size) {
      if (seq.size() < size && tree != null && tree.next.size() > 0) {
    	int rand = (int)(Math.random() * tree.next.size());
	    seq.add(tree.next.get(rand).note);
	    chooseNoteFromTree(tree.next.get(rand), seq, size);
      }
    }

//  public double calcFitness(List<Integer> s, List<ObservationReal> o, List<MusicElement> e) {
//    return calcFitness(s, o.stream().map(it -> it.value).collect(Collectors.toList()), e);
//  }
//  @Override
//  public double calcFitness(List<Integer> s, List<ObservationReal> o, List<MusicElement> e) {
//    return 0;
//  }
@Override
public double calcFitness(List<Integer> s, List<Double> o, List<MusicElement> e) {
//  System.out.println("calcFitness called " + s);
  double mse = -calcRMSE(s, o);
  double lik = calcLogTransLikelihood(s);
  double delta = calcLogDeltaTransLikelihood(s);
  double entr = calcEntropy(s, 12);
  double entrdiff = -(entr-entropy_mean) * (entr-entropy_mean);
  double chord = calcLogChordBeatUnigramLikelihood(s, e);
  return 3 * mse + 2 * lik + 1 * delta + 3 * chord + 20 * entrdiff;
}

//    public double calcFitness(List<Integer> s, List<ObservationReal> o, List<MusicElement> e) {
//      System.out.println("calcFitness called " + o.get(0).getClass());
//      List<Double> dList = new ArrayList<Double>();
//      o.forEach(it -> dList.add(it.value));
//      double mse = -calcRMSE(s, o.stream().map(it -> it.value).collect(Collectors.toList()));
//      double lik = calcLogTransLikelihood(s);
//      double delta = calcLogDeltaTransLikelihood(s);
//      double entr = calcEntropy(s, 12);
//      double entrdiff = -(entr-entropy_mean) * (entr-entropy_mean);
//      double chord = calcLogChordBeatUnigramLikelihood(s, e);
//      return 3 * mse + 2 * lik + 1 * delta + 3 * chord + 20 * entrdiff;
//      //      3 * mse + 2 * lik + delta + 3 * chord + 10 * entrdiff
//    }

    double calcRMSE(List<Integer> s, List<Double> o) {
//      System.out.println("calcRMSE called! " + o);
      double e = 0;
      int length = s.size();
      for (int i = 0; i < length; i++) {
	    double e1 = (o.get(i) % 12) - (s.get(i) % 12);
	    e += e1 * e1;
      }
      return Math.sqrt(e);
    }

    double calcLogTransLikelihood(List<Integer> s) {
      double lik = 0.0;
      int length = s.size() - 1;
      for (int i = 0; i < length; i++) {
    	lik += Math.log(bigram.get(s.get(i)).get(s.get(i + 1)).doubleValue());
      }
      return lik;
    }

    double calcLogDeltaTransLikelihood(List<Integer> s) {
      double lik = 0.0;
      int length = s.size() - 2;
      for (int i = 0; i < length; i++) {
//        System.out.println("(" + s.get(i + 1) + "-" + s.get(i) + "), (" + s.get(i + 2) + "-" + s.get(i + 1) + "), " + s.size());
        int j = (s.get(i + 1) - s.get(i)) >= 0 ? (s.get(i + 1) - s.get(i)) % s.size() : (s.get(i + 1) - s.get(i)) % s.size() + s.size();
        int k = (s.get(i + 2) - s.get(i + 1)) >= 0 ? (s.get(i + 2) - s.get(i + 1)) % s.size() : (s.get(i + 2) - s.get(i + 1)) % s.size() + s.size();
//	lik += Math.log(delta_bigram.get(s.get(i + 1) - s.get(i)).get(s.get(i + 2) - s.get(i + 1)).doubleValue());
//        System.out.println(delta_bigram.get(j).get(k) + ",  " + delta_bigram.get(j).get(k).getClass()); +

//        System.out.println(delta_bigram.get(j).get(k).getClass() + " " + delta_bigram.get(j).get(k));

        if (delta_bigram.get(j).get(k).equals(0)) {
          lik += Math.log(BigDecimal.ZERO.doubleValue());
        } else {
          lik += Math.log(((BigDecimal)delta_bigram.get(j).get(k)).doubleValue());
        }
      }
      return lik;
    }

    double calcLogChordBeatUnigramLikelihood(List<Integer> s,
					     List<MusicElement> e) {
      double lik = 0.0;
      int length = s.size() - 1;
      for (int i = 0; i < length; i++) {
        String chord = chordprog.get((int) i / DIVISION).toString();
        int div4 =(int) DIVISION / BEATS_PER_MEASURE;
        String beat = (e.get(i).tick() == 0 ? "head" :
                   (e.get(i).tick() % div4 == 0 ? "on" : "off"));
        String dur = (e.get(i).duration() >= div4 ? "long" : "short");
        String key = chord + "_" + beat + "_" + dur;
        lik += Math.log(chord_beat_dur_unigram.get(key).get(s.get(i)).doubleValue() + 0.001);
      }
      return lik;
    }

    double calcEntropy(List<Integer> s, int maxvalue) {
//      int[] freq = [0] * maxvalue;
      List<Integer> freq = new ArrayList<Integer>();
      IntStream.rangeClosed(0, maxvalue).forEach(i -> freq.add(0));

      int sum = 0;
      for (int i = 0; i < s.size(); i++) {
	    freq.set(s.get(i), 1);
        sum++;
	  }
      double entropy = 0.0;
      for (int i = 0; i < maxvalue; i++) {
	if (freq.get(i) > 0) {
	  double p = (double) freq.get(i) / sum;
	  entropy += -Math.log(p) * p / Math.log(2);
	}
      }
      return entropy;
    }
  }

}


