package jp.kthrlab.jamsketch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.transform.TransformerException;

import jp.crestmuse.cmx.elements.MutableMusicEvent;
import jp.crestmuse.cmx.filewrappers.SCC;
import jp.crestmuse.cmx.filewrappers.SCCDataSet;
import jp.crestmuse.cmx.filewrappers.SCCUtils;
import jp.crestmuse.cmx.inference.MusicCalculator;
import jp.crestmuse.cmx.inference.MusicElement;
import jp.crestmuse.cmx.inference.MusicRepresentation;
import jp.crestmuse.cmx.processing.CMXApplet;
import jp.crestmuse.cmx.processing.gui.SimplePianoRoll;
import jp.kshoji.javax.sound.midi.impl.SequencerImpl;

public class MelodyData {
  public List<Integer> curve1; // y coord per pixel
  public List<Integer> curve2; // note number per beat
//  def RHYTHMS = [[1, 0, 0, 0, 0, 0], [1, 0, 0, 1, 0, 0],
//		 [1, 0, 0, 1, 0, 1], [1, 0, 1, 1, 0, 1],
//		 [1, 0, 1, 1, 1, 1], [1, 1, 1, 1, 1, 1]];
  List<List<Integer>> RHYTHMS = new ArrayList<List<Integer>>(
          Arrays.asList(new ArrayList<Integer>(Arrays.asList(1, 0, 0, 0, 0, 0)),
                  new ArrayList<Integer>(Arrays.asList(1, 0, 0, 1, 0, 0)),
                  new ArrayList<Integer>(Arrays.asList(1, 0, 0, 1, 0, 1)),
                  new ArrayList<Integer>(Arrays.asList(1, 0, 1, 1, 0, 1)),
                  new ArrayList<Integer>(Arrays.asList(1, 0, 1, 1, 1, 1)),
                  new ArrayList<Integer>(Arrays.asList(1, 1, 1, 1, 1, 1))));
  // The size of each list has to be DIVISION
  SCCDataSet scc;
  SCCDataSet.Part target_part;
  MelodyModel model;
//  def expgen;
  int width;
  SimplePianoRoll pianoroll;
  CMXApplet cmx;

  public  MelodyData(String filename, int width, CMXApplet cmxcontrol, SimplePianoRoll pianoroll) {
      cmx = cmxcontrol;
      this.width = width;
    this.pianoroll = pianoroll;
    curve1 = new ArrayList<Integer>(width); //Collections.nCopies(width, new Integer(0)); //[null] * width;
      IntStream.range(0, width).forEach(i -> curve1.add(i, null));

      try {
//          scc = cmxcontrol.readSMFAsSCC(getClass().getResourceAsStream(filename)).toDataSet();
          scc = cmxcontrol.readSMFAsSCC(JamSketchActivity.getMyResources().getAssets().open(filename)).toDataSet();
      } catch (TransformerException e) {
          e.printStackTrace();
      } catch (Exception e) {
          e.printStackTrace();
      }
      scc.repeat(Config.INITIAL_BLANK_MEASURES * Config.BEATS_PER_MEASURE * scc.getDivision(),
	       (Config.INITIAL_BLANK_MEASURES + Config.NUM_OF_MEASURES) *
                   Config.BEATS_PER_MEASURE * scc.getDivision(), Config.REPEAT_TIMES - 1);
    target_part = scc.getFirstPartWithChannel(1);
    model = new MelodyModel(cmxcontrol, new SCCGenerator());
//    if (R.bool.EXPRESSION) {
//      expgen = new ExpressionGenerator();
//      expgen.start(scc.getFirstPartWithChannel(1),
//     		 getFullChordProgression(), CFG.BEATS_PER_MEASURE);
//    }
  }

  void resetCurve() {
//    curve1.clear(); // = [null] * width;
      curve1.clear();
      IntStream.range(0, width).forEach(i -> curve1.add(i, null));
    updateCurve("all");
  }
    
  void updateCurve(Object measure) {
    List curve1Sublist = curve1.subList(100, curve1.size());
//      curve1Sublist.stream().forEach(it -> System.out.println("curve1Sublist:y:" + it));
      List curveNotenum = (List) curve1Sublist.stream()
            .map(it -> it == null ? null : (int)pianoroll.y2notenum(((Integer)it).doubleValue()))
              .collect(Collectors.toList());

//      curveNotenum.stream().forEach(it -> System.out.println("curveNotenum:" + it));

    curve2 = shortenCurve(curveNotenum);

//    curve2.stream().forEach(it -> System.out.println("curve2:" + it));

    setDataToMusicRepresentation(curve2, model.mr);
    if ("all".equals(measure)) {
//      (0..< R.integer.NUM_OF_MEASURES).each {
        IntStream.range(0, Config.NUM_OF_MEASURES).forEach(i -> {
            model.updateMusicRepresentation(i);
        });
//        for (int i = 0; i < res.getInteger(R.integer.NUM_OF_MEASURES); i++) {
//	    model.updateMusicRepresentation(i);
//        }
    } else {
        model.updateMusicRepresentation((Integer) measure);
    }
  }

  void setDataToMusicRepresentation(List curve, MusicRepresentation mr) {
//      System.out.println("setDataToMusicRepresentation called!");
//      curve.stream().forEach(it -> {System.out.println(it);});

      List rhythm = decideRhythm(curve2, 0.25);
//    (0..<(CFG.NUM_OF_MEASURES * CFG.DIVISION)).each { i ->
      IntStream.range(0, Config.NUM_OF_MEASURES * Config.DIVISION).forEach(i -> {
          MusicElement e = mr.getMusicElement("curve", i / Config.DIVISION,
                  i % Config.DIVISION);
          e.suspendUpdate();
          if (curve.get(i) == null) {
              e.setRest(true);
          } else if (rhythm.get(i).equals(0)) {
              e.setRest(false);
              e.setTiedFromPrevious(true);
          } else {
              e.setRest(false);
              e.setTiedFromPrevious(false);
              e.setEvidence(curve.get(i));
//              System.out.println("setEvidence(" + curve.get(i) + ")");
          }
      });
  }

  List<Integer> shortenCurve(List<Integer> curve) {
//      System.out.println("curve.size()=" + curve.size());
    List<Integer> shortCurve = new ArrayList();
    int measureByDiv = Config.NUM_OF_MEASURES * Config.DIVISION;
    IntStream.range(0, measureByDiv).forEach (i -> {
                int from = (int)(((double)i / (double)measureByDiv) * curve.size());
                int thru = (int)(((double)(i+1) / (double)measureByDiv) * curve.size());
//                System.out.println("i=" + i + ", from=" + from + ", thru=" + thru);
//                curve.subList(from, thru).stream().forEach(it -> System.out.println(it));
                List<Integer> notNullCurve = (List<Integer>) curve.subList(from, thru)
                                                    .stream()
                                                    .filter(it -> it !=null)
                                                    .collect(Collectors.toList());

                if (notNullCurve.size() > 0)
                    shortCurve.add(
                            notNullCurve.stream()
                                    .filter(Objects::nonNull)
                                    .reduce(Integer::sum)
                                    .get()
//                                    .orElse(null)
                                    / notNullCurve.size());
                else
                    shortCurve.add(null);
            });
//    (0..<(R.integer.NUM_OF_MEASURES * R.integer.DIVISION)).each { i ->

    //}
    return shortCurve;
  }
    
  List decideRhythm(List curve, double th) {
    //def r1 = [1];
    List<Integer> r1 = new ArrayList<Integer>(Arrays.asList(1));
//    (0..<(curve.size()-1)).each { i ->
      IntStream.range(0, curve.size()-1).forEach(i -> {
          if (curve.get(i+1) == null) {
              r1.add(0);
          } else if (curve.get(i) == null && curve.get(i+1) != null) {
              r1.add(1);
          } else if (Math.abs((int)curve.get(i+1) - (int)curve.get(i)) >= th) {
              r1.add(1);
          } else {
              r1.add(0);
          }
      });
//      for (int i = 0; i < curve.size() -1; i++) {
//    }
    List<List<Integer>> r2 = new ArrayList();
//    (0..<(CFG.NUM_OF_MEASURES*2)).each { i ->
      IntStream.range(0, Config.NUM_OF_MEASURES*2).forEach(i -> {
          List<Integer> diffs = new ArrayList<Integer>();
          List<Integer> r1sub = r1.subList(Config.DIVISION/2*i, Config.DIVISION/2*(i+1)); //[(R.integer.DIVISION/2*i)..< (R.integer.DIVISION/2*(i+1))];
          for (List<Integer> r : RHYTHMS) {
              List<Integer> absList = abs(sub(r, r1sub));
              diffs.add(absList.stream().mapToInt(Integer::intValue).sum());
          }
//          RHYTHMS.stream()
//                  .map(r -> abs(sub(r, r1sub)))
//                  .forEach(r -> {
//                    diffs.add(r.stream().reduce(Integer::sum).get());
//                  });
//          diffs.stream().forEach(it -> System.out.println("diffs:" + it));
          int index = argmin(diffs);
          r2.add(RHYTHMS.get(index));
      });
      return r2.stream().flatMap(r -> r.stream()).collect(Collectors.toList());
  }

  List<Integer> sub(List<Integer> x, List<Integer> y) {
    List z = new ArrayList();
    IntStream.range(0, ((List)x).size()).forEach(i -> z.add((int)((List)x).get(i) - y.get(i)));
//    x.indices.each{ i ->
//      z.add(x[i] - y[i]);
//    }
    return z;
  }
    
  List<Integer> abs(List<Integer> x) {
    return x.stream().map(it -> Math.abs(it)).collect(Collectors.toList());
    //x.collect{ Math.abs(it) }
  }
    
  int argmin(List<Integer> x) {
//      System.out.println("argmin x.size()=" + x.size());
    int minvalue = x.get(0);
    int argmin = 0;
    //x.indices.each { i ->
      for (int i = 0; i < x.size(); i++) {
          if (x.get(i) < minvalue) {
              minvalue = x.get(i);
              argmin = i;
          }
      }
//    minvalue = x.stream()
//            .reduce(Integer::min)
//            .get();
//    return x.indexOf(minvalue);
      return argmin;
  }

//  def getFullChordProgression() {
//    return [NON_CHORD] * R.integer.INITIAL_BLANK_MEASURES + CFG.chordprog * CFG.REPEAT_TIMES;
//  }

  class SCCGenerator implements MusicCalculator {
    public void updated(int measure, int tick, String layer, MusicRepresentation mr) {
        System.out.println("measure: " + measure + ", tick: " + tick);
      int sccdiv = scc.getDivision();
      int firstMeasure = pianoroll.getDataModel().getFirstMeasure();
      MusicElement e = mr.getMusicElement(layer, measure, tick);
      if (!e.rest() && !e.tiedFromPrevious()) {
        int notenum = getNoteNum((Integer) e.getMostLikely(), curve2.get(measure * Config.DIVISION + tick));
        int duration = e.duration() * sccdiv / (Config.DIVISION / Config.BEATS_PER_MEASURE);
        int onset = ((firstMeasure + measure) * Config.DIVISION + tick) * sccdiv / (Config.DIVISION / Config.BEATS_PER_MEASURE);


        if (onset >  cmx.getTickPosition() && onset+duration <= cmx.getSequencer().getTickLength()) {

            // add synchronized 20190619 fujii
            synchronized(target_part) {
                List<SCC.Note> oldnotes =
                        SCCUtils.getNotesBetween(target_part, onset,
                                onset+duration, sccdiv, true, true);
                //data.target_part.getNotesBetween2(onset, onset+duration)

                oldnotes.stream().forEach(n -> target_part.remove((MutableMusicEvent) n));
//          target_part.remove((List<MutableMusicEvent>)oldnotes);

                target_part.addNoteElement(onset, onset+duration, notenum, 100, 100);

                // add for debug 20190612 fujii
                ((SequencerImpl)cmx.getSequencer()).refreshPlayingTrack();

            }
        }
      }

//      if (R.bool.EXPRESSION) {
//	def fromTick = (firstMeasure + measure) * CFG.BEATS_PER_MEASURE * CFG.DIVISION;
//	def thruTick = fromTick + CFG.BEATS_PER_MEASURE * CFG.DIVISION;
//	expgen.execute(fromTick, thruTick, CFG.DIVISION);
//      }
    }

    int getNoteNum(int notename, int neighbor) {
      AtomicInteger best = new AtomicInteger();
//      for (int i in 0..11) {
        IntStream.rangeClosed(0, 11).forEach(i -> {
            int notenum = i * 12 + notename;
            if (Math.abs(notenum - neighbor) < Math.abs(best.get() - neighbor))
                best.set(notenum);

        });
//      }
      return best.get();
    }
  }
}
