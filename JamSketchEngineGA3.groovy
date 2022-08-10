@Grapes([
    @Grab(group='org.tensorflow', module='tensorflow-core-api', version='0.4.1'),
    @Grab(group='org.tensorflow', module='tensorflow-core-platform', version='0.4.1'),
    @Grab(group='org.bytedeco', module='javacpp', version='1.5.7'),
    @Grab(group='org.tensorflow', module='ndarray', version='0.3.3'),
    @Grab(group='com.google.truth', module='truth', version='1.0.1', scope='test')

])

import org.apache.commons.math3.genetics.*
import jp.crestmuse.cmx.inference.*
import jp.crestmuse.cmx.inference.models.*
import static java.lang.Double.*
import org.tensorflow.SavedModelBundle
import org.tensorflow.ndarray.Shape
import org.tensorflow.ndarray.NdArray
import org.tensorflow.ndarray.NdArrays
import org.tensorflow.ndarray.IntNdArray;
import org.tensorflow.types.TInt32;


class JamSketchEngineGA3 extends JamSketchEngineAbstract {
  int lastUpdateMeasure = -1
  long lastUpdateTime = -1
  SavedModelBundle TFmodel = null

  double RHYTHM_THRESHOLD = 0.08
//  double RHYTHM_THRESHOLD = 0.25S
  
  //set Model to TFmodel Variable.
  def setTFModel() {
    try {
        def TFmodel_file = new File("./onebar_model")
        def TFmodel_path = TFmodel_file.getPath()
        TFmodel= SavedModelBundle.load(TFmodel_path,"serve")
    }catch(IOException e){
        System.err.println "Model not founded"
    }
 }

//preprocessing input data
 def preprocessing(int measure) {

  def nn_from = 36
  def tf_row= 16
  def measures_num = 16
  def tf_column = 133
  

  IntNdArray tf_input =  NdArrays.ofInts(Shape.of(tf_row, tf_column))
  // initialize tf_input to set 0;
  for(i in 0..<tf_row){
    for(j in 0..<tf_column){
      tf_input.setInt(0, i, j)
    }}


  def mes = mr.getMusicElementList("curve")
  def me_start = (mes.size()/12)*measure
  def me_end   = me_start + 16
  def me_per_measure = mes[me_start ..<me_end]




  for (i in 0..<measures_num) {
    def note_num_f=me_per_measure[i].getMostLikely()
    if(note_num_f !=NaN){
      def note_number = Math.floor(note_num_f)-nn_from
      tf_input.setInt(1, i, (int)note_number)
   
    }else{
      tf_input.setInt(1, i, 121)
    }

  }

  return tf_input
    // for (i in 0..<16){
    //   print me_per_measure[i].getMostLikely()+", "
    // }
    // for(i in 0..<tf_row){
    //  for(j in 0..<tf_column){
    //    println i+"line "+j+"columns: "+tf_input.getInt(i, j)
    //   }
    // }
 }

 def prediction(IntNdArray tf_input) {
  TInt32 ten_input = TInt32.tensorOf(tf_input)
  TInt32 ten_output = (TInt32)TFmodel.session()
                        .runner()
                        .feed("serving_default_conv2d_13_input", ten_input)
                        .fetch("StatefulPartitionedCall")
                        .run()
                        .get(0)
  return output_ten
 }


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
    setTFModel()
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
    IntNdArray tf_input
    IntNdArray tf_output
    if (tick == cfg.DIVISION - 1 &&
        lastUpdateMeasure != measure &&
	currentTime - lastUpdateTime >= 100000) {

      applyRhythm(measure, decideRhythm(measure, RHYTHM_THRESHOLD))
      mr.getMusicElement(OUTLINE_LAYER, measure, tick).resumeUpdate()
      lastUpdateMeasure = measure
      lastUpdateTime = currentTime
      //get OUTLINE_LAYER Elements and Model the data for it to be inserted into model.
      tf_input = preprocessing(measure)
      tf_output = prediction(tf_input)
      println tf_output

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