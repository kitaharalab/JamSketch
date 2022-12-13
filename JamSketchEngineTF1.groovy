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

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

class JamSketchEngineTF1 extends JamSketchEngineAbstract {

def CHORD_VECTORS = [
  "C": [1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0], 
  "F": [1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0], 
  "G": [0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1]
]




  int lastUpdateMeasure = -1
  long lastUpdateTime = -1
  SavedModelBundle TFmodel = null

  
  //Set Model to TFmodel Variable.
  def init_local() {
        def TFmodel_file = new File(cfg.TF_MODEL_ENGINE)
        def TFmodel_path = TFmodel_file.getPath()
        TFmodel= SavedModelBundle.load(TFmodel_path)
 }



//preprocessing input data
 def preprocessing(int measure) {

 

  def nn_from = cfg.TF_NOTE_NUM_START
  def tf_row= cfg.DIVISION
  def tf_column = cfg.TF_MODEL_INPUT_COL

  FloatNdArray tf_input =  NdArrays.ofFloats(Shape.of(1 , tf_row, tf_column, 1))
  // initialize tf_input;
  for(i in 0..<tf_row){
    for(j in 0..<tf_column){
      tf_input.setFloat(0.0f, 0, i, j, 0)
    }
  }

  def mes = mr.getMusicElementList("curve")
  def me_start = (mes.size()/cfg.NUM_OF_MEASURES)*measure
  def me_end   = me_start + cfg.DIVISION
  def me_per_measure = mes[me_start ..<me_end]

  for (i in 0..<tf_row) {
    def note_num_f=me_per_measure[i].getMostLikely()
    if(note_num_f !=NaN){
      def note_number = Math.floor(note_num_f)-nn_from
      tf_input.setFloat(1.0f, 0,i, note_number as int,0)

   
    }else{
      tf_input.setFloat(1.0f, 0, i, cfg.TF_REST_COL, 0)
    }

    //set chrod
    def chord_column_from = cfg.TF_CHORD_COL_START
    def chord = getChord(measure, i)
    def chord_vec = CHORD_VECTORS[chord.toString()]

    for (j in 0..<cfg.TF_NUM_OF_MELODY_ELEMENT) {
      float chord_value = chord_vec[j] 
      tf_input.setFloat(chord_value, 0, i, chord_column_from + j, 0)
    }
  }

  return tf_input

 }

 //Predict the output by TFmodel.
 def predict(FloatNdArray tf_input) {
  TFloat32 ten_input = TFloat32.tensorOf(tf_input)
  TFloat32 ten_output = (TFloat32)TFmodel.session()
                        .runner()
                        .feed(cfg.TF_MODEL_ENGINE_LAYER, ten_input)
                        .fetch("StatefulPartitionedCall")
                        .run()
                        .get(0)
  // return ten_output
  return ten_output
 }

 def exportCVS(FloatNdArray data, String logfile) {

  def tf_column = (int)data.shape().size(2)
  def tf_row= (int)data.shape().size(1)


 
  try {

    FileWriter fw = new FileWriter(logfile, false)
    PrintWriter pw = new PrintWriter(new BufferedWriter(fw))
    
    for (i in 0..<tf_row) {
      for (j in 0..<tf_column) {
        pw.print(data.getFloat(0,i,j,0))
        pw.print(",")
      }
      pw.println()
    }
    pw.close();
    System.out.println("Save as " + logfile);
 
  } catch (IOException ex) {
    ex.printStackTrace();
  }
 }


//Normalize the prediction data which if it is under 0.5, be 0, if it is over 0.5, be 1.
 def normalize(TFloat32 tf_output) {
  def tf_row=cfg.DIVISION
  def tf_column=cfg.TF_MODEL_OUTPUT_COL

  for(i in 0..<tf_row) {
    for(j in 0..<tf_column) {
      def value = tf_output.getFloat(0,i,j,0)
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

  def tf_row=cfg.DIVISION
  def tf_column=cfg.TF_MODEL_OUTPUT_COL
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

 def setEvidences(Integer measure, Integer lastTick, List note_num_list) {
  for (i in 0..lastTick) {    
    def note_num = note_num_list[i]
    def e = mr.getMusicElement("melody", measure, i)
    if (note_num == "rest") {
      e.setRest(true)
    } else {
      e.setRest(false)
      if (note_num >= cfg.TF_NOTE_CON_COL_START && (i>=1 && note_num instanceof Integer && note_num_list[i-1] instanceof Integer && (note_num % cfg.TF_NUM_OF_MELODY_ELEMENT) == (note_num_list[i-1] % cfg.TF_NUM_OF_MELODY_ELEMENT))) {
        e.setTiedFromPrevious(true)
        println("Tied: ${i}")
      }
    }
  }
  for (i in 0..lastTick) {
    def note_num = note_num_list[i]
    def e = mr.getMusicElement("melody", measure, i)
    if (note_num instanceof Integer && !e.tiedFromPrevious()) {
      e.setEvidence(note_num % cfg.TF_NUM_OF_MELODY_ELEMENT)
    }
  }

 }

  Map<String,Double> parameters() {
    return [:]
  }

  Map<String,String> paramDesc() {
    return [:]
  }
  
  def musicCalculatorForOutline() {
    return null
  }

  def outlineUpdated(measure, tick) {


    // println("outlineUpdated: " + measure + " " + tick)
    long currentTime = System.nanoTime()
    if (//tick == cfg.DIVISION - 1 &&
        //lastUpdateMeasure != measure &&
	      currentTime - lastUpdateTime >= 1000 * 1000 * 150) {
      String logname=""
      String date=""
      mr.getMusicElement(OUTLINE_LAYER, measure, tick).resumeUpdate()
      lastUpdateMeasure = measure
      lastUpdateTime = currentTime
      
      //get OUTLINE_LAYER Elements and Model the data for it to be inserted into model.
      FloatNdArray tf_input = preprocessing(measure)

      if (cfg.DEBUG) {
        date=(new Date()).toString().replace(" ", "_").replace(":", "-")+".csv"
        logname="./log/" + "tf_input" + date
        exportCVS(tf_input, logname)
      }
      FloatNdArray tf_output = predict(tf_input)
    

      if (cfg.DEBUG) {
        logname="./log/" + "tf_output" + date
        exportCVS(tf_output, logname)
      }

      FloatNdArray normalized_data= normalize(tf_output)
      def label_List = getLabelList(normalized_data)
      // println label_List
      setEvidences(measure, tick, label_List)

    }
  }

  def automaticUpdate() {
    false
  }
  
}