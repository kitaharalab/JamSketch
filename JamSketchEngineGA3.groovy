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
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.types.TFloat32;
import jp.crestmuse.cmx.misc.*


class JamSketchEngineGA3 extends JamSketchEngineAbstract {

def CHORD_VECTORS = [
  "C": [1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0], 
  "F": [1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0], 
  "G": [0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1]
]

  int lastUpdateMeasure = -1
  long lastUpdateTime = -1
  SavedModelBundle TFmodel = null

  double RHYTHM_THRESHOLD = 0.08
//  double RHYTHM_THRESHOLD = 0.25S
  
  //Set Model to TFmodel Variable.
  def setTFModel() {
    try {
          def TFmodel_file = new File("./onebar_model")
        def TFmodel_path = TFmodel_file.getPath()
        TFmodel= SavedModelBundle.load(TFmodel_path)
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

  FloatNdArray tf_input =  NdArrays.ofFloats(Shape.of(1 , tf_row, tf_column, 1))
  // initialize tf_input;
  for(i in 0..<tf_row){
    for(j in 0..<tf_column){
      tf_input.setFloat(0.0f, 0, i, j, 0)
    }
  }

  def mes = mr.getMusicElementList("curve")
  def me_start = (mes.size()/12)*measure
  def me_end   = me_start + 16
  def me_per_measure = mes[me_start ..<me_end]

  for (i in 0..<measures_num) {
    def note_num_f=me_per_measure[i].getMostLikely()
    if(note_num_f !=NaN){
      def note_number = Math.floor(note_num_f)-nn_from
      tf_input.setFloat(1.0f, 0,i, (int)note_number,0)
   
    }else{
      tf_input.setFloat(1.0f, 0, i, 121, 0)
    }

    //set chrod
    def chord_column_from = 121
    def chord = getChord(measure, i)
    def chord_vec = CHORD_VECTORS[chord.toString()]
    for (j in 0..<12) {
      float chord_value = chord_vec[j] 
      tf_input.setFloat(chord_value, 0, i, chord_column_from + j, 0)
    }
/*
    for(i2 in 0..<12tf_row){
      def chord_column=121
      for(_ in 0..<12){
        chord_column+=1
        switch(chord_column) {
          case 123:
            tf_input.setFloat(1.0f, 0, i2, chord_column, 0)
            break
          case 127:
            tf_input.setFloat(1.0f, 0, i2, chord_column, 0)
            break
          case 130:
            tf_input.setFloat(1.0f, 0, i2, chord_column, 0)
            break
          case 132:
            tf_input.setFloat(1.0f, 0, i2, chord_column, 0)
            break
        }
    }
  }
*/
  }

  return tf_input

 }

 //Predict the output by TFmodel.
 def predict(FloatNdArray tf_input) {
  TFloat32 ten_input = TFloat32.tensorOf(tf_input)
  TFloat32 ten_output = (TFloat32)TFmodel.session()
                        .runner()
                        .feed("serving_default_conv2d_13_input", ten_input)
                        .fetch("StatefulPartitionedCall")
                        .run()
                        .get(0)
  // return ten_output
  return ten_output
 }


//Normalize the prediction data which if it is under 0.5, be 0, if it is over 0.5, be 1.
 def normalize(TFloat32 tf_output) {
  def tf_row=16
  def tf_column=121
  def value=0

  for(i in 0..<tf_row) {
    for(j in 0..<tf_column) {
      value = tf_output.getFloat(0,i,j,0)
      if(value >= 0.5f){
        tf_output.setFloat(1.0f,0,i,j,0)
      }else{

        tf_output.setFloat(0.0f,0,i,j,0)
      }

    }
  }
   return tf_output

 }
 //Get the labels from the predicton data as Integer 1 to 11.
 def getLabelList(TFloat32 tf_output) {
  def tf_row=16
  def tf_column=120
  def note_num_list=[]

  for(i in 0..<tf_row){
    def isSet=false

    for(j in 0..<tf_column) {
      if(tf_output.getFloat(0,i,j,0)==1.0f){
        note_num_list.add(j)
        isSet=true
      }
    }
    //If all columns is 0, add  "rest" in  note_num_list.
    if(!isSet){
      note_num_list.add("rest")
    }
  }


  return note_num_list

 }

 def setEvidenceses(Integer measure, Integer lastTick, List note_num_list) {
  for (i in 0..lastTick) {    
    def note_num = note_num_list[i]
    def e = mr.getMusicElement("melody", measure, i)
    if (note_num == "rest") {
      e.setRest(true)
    } else {
      e.setRest(false)
      if (note_num >= 60 && (i>=1 && note_num instanceof Integer && note_num_list[i-1] instanceof Integer && (note_num % 12) == (note_num_list[i-1] % 12))) {
        e.setTiedFromPrevious(true)
        println("Tied: ${i}")
      }
    }
  }
  for (i in 0..lastTick) {
    def note_num = note_num_list[i]
    def e = mr.getMusicElement("melody", measure, i)
    if (note_num instanceof Integer && !e.tiedFromPrevious()) {
      e.setEvidence(note_num % 12)
    }
  }
/*
  for (i in 0..lastTick){
    def note_num = note_num_list[i]
    def e = mr.getMusicElement("melody", measure ,i)

    if(note_num=="rest") {
      e.setRest(true)
    } else if (note_num >= 60 && (i>=1 && note_num instanceof Integer && note_num_list[i-1] instanceof Integer && (note_num % 12) == (note_num_list[i-1] % 12))) {
      e.setTiedFromPrevious(true)
      println("Tied: ${i}")
    } else {
      e.setTiedFromPrevious(false)
      e.setEvidence(note_num % 12)
    }
    
  }
*/
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
    /*
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
*/
    return null
  }

  def outlineUpdated(measure, tick) {
    long currentTime = System.nanoTime()
    FloatNdArray tf_input
    FloatNdArray tf_output
    FloatNdArray normalized_data
    def label_List=[]
    if (tick == cfg.DIVISION - 1 &&
        lastUpdateMeasure != measure &&
	      currentTime - lastUpdateTime >= 100000) {

//      applyRhythm(measure, decideRhythm(measure, RHYTHM_THRESHOLD))
      mr.getMusicElement(OUTLINE_LAYER, measure, tick).resumeUpdate()
      lastUpdateMeasure = measure
      lastUpdateTime = currentTime
      //get OUTLINE_LAYER Elements and Model the data for it to be inserted into model.
      tf_input = preprocessing(measure)
      tf_output = predict(tf_input)
      normalized_data= normalize(tf_output)
      label_List = getLabelList(normalized_data)
      // println label_List
      setEvidenceses(measure, tick, label_List)

      


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