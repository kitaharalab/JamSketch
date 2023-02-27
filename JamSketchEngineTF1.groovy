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
import org.tensorflow.internal.types.TFloat32Mapper
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
    System.out.println("Save as ${logfile}");
 
  } catch (IOException ex) {
    ex.printStackTrace();
  }
 }

//  def inputCSV(String filepath) {
//   FloatNdArray tf_input =  NdArrays.ofFloats(Shape.of(1, cfg.DIVISION, cfg.TF_MODEL_INPUT_COL, 1))

//     try (BufferedReader br = new BufferedReader(new FileReader(filepath))){
//       def csv_row = 0
//       def tf_row = 0
//       def line
      
//       while((line = br.readLine()) != null && csv_row <= (cfg.DIVISION-1)) {
//         def values_str = line.split(",")

//         values_str.eachWithIndex { value, column ->
//           tf_input.setFloat(value.toFloat(), 0, tf_row, column, 0)
//         }
  
//         tf_row += 1
//         csv_row += 1
//       }
        
//       } catch (IOException e) {
//       e.printStackTrace()
//     }

//     return tf_input
//  }

//Normalize the prediction data which if it is under 0.5, be 0, if it is over 0.5, be 1.
 def normalize(TFloat32 tf_output) {
  
  """

  入力: 
      tf_output: 16x121の行列(predict後の行列)
  
  内部で使用している変数:
      tmp_output: tf_outputの60~120列目までのデータを格納。
    
  出力:
      tf_output: 0.5以上を1、0.5より小さい値のものを0に置き換えた行列。
  
  """

  // 小節の個数を取得。(16)
  def tf_row=cfg.DIVISION
  // 行列(16×121)の列数を取得。(121)
  def tf_column=cfg.TF_MODEL_OUTPUT_COL

  // 16×60の行列を作成。 
  FloatNdArray tmp_output =  NdArrays.ofFloats(Shape.of(1 , tf_row, (tf_column-1)/2 as int, 1)) 
  // tf_ouputの全ての行の60~120列目(結合部分)をtmp_outputに代入。
  for(i in 0..<tf_row) {
    for(j in 60..<tf_column-1) {
      tmp_output.setFloat(tf_output.getFloat(0, i, j, 0), 0, i, j-60, 0)
    }
  }

  // 24measure_MelodyGenerationDemo.ipynbの関数(make_note_msgs)の処理に対応。
  for(i in 0..<tf_row) {
    for(j in 0..<(tf_column-1)/2) {
      if ((tf_output.getFloat(0,i,j,0) <= 0.5) && (tmp_output.getFloat(0,i,j,0) > 0.5)) {
        if (i >= 1 && tmp_output.getFloat(0, i-1, j, 0) < 0.5 && tf_output.getFloat(0, i-1, j ,0 ) < 0.5){
          //値が0.5以上になるように加算。
          tf_output.setFloat((tmp_output.getFloat(0, i, j, 0)+tf_output.getFloat(0, i, j, 0)) as float, 0, i, j, 0)
          tf_output.setFloat(0.0, 0, i, j+60, 0)
        }
      }
    }
  }
 // tf_outputの各値に対して,0.5以上であれば1.0、0.5以下であれば0に置き換える。
  for(i in 0..<tf_row) {
    for(j in 0..<tf_column){
      
      if(tf_output.getFloat(0, i, j, 0) >= 0.5) {

        tf_output.setFloat(1.0, 0, i, j, 0)

      } else {

        tf_output.setFloat(0.0, 0, i, j, 0)
      }
    }
  }


   return tf_output
}


 def setEvidences(Integer measure, Integer lastTick, TFloat32 tf_normalized) {
  """
  入力: 
      tf_normalized: 16x121の0または1を値として持つ行列。
      measure: 小節。
      lasttick: 音符。
  
  内部で使用している変数:
      tf_normalized2: tf_normalizedの60~120列目までのデータを格納。
    
  出力:
      tf_output: 0.5以上を1、0.5より小さい値のものを0に置き換えた行列。

  """


  // 小節の個数を取得。(16)
  def tf_row=cfg.DIVISION
  // 行列(16×121)の列数を取得。(121)
  def tf_column=cfg.TF_MODEL_OUTPUT_COL
  // 16×60の行列を作成。 
  FloatNdArray tf_normalized2 =  NdArrays.ofFloats(Shape.of(1 , tf_row, (tf_column-1)/2 as int, 1))
  // tf_ouputの全ての行の60~120列目(結合部分)をtmp_outputに代入。
  for(i in 0..<tf_row) {
    for(j in 60..<tf_column-1) {
      tf_normalized2.setFloat(tf_normalized.getFloat(0, i, j, 0), 0, i, j-60, 0)
    }
  }


  for (i in 0..lastTick) 
    //MusicElement要素を取得。
    def e = mr.getMusicElement("melody", measure, i)

    //121列目の値が1.0だったら休符を設定。
    if (tf_normalized.getFloat(0, i, 120, 0) == 1.0) {
      e.setRest(true)
    } else {

      e.setRest(false)

      if (i >= 1) {
        for (j in 0..<(tf_column-1)/2) {
          if((tf_normalized2.getFloat(0,i-1,j,0) == 1.0 || tf_normalized.getFloat(0,i-1,j,0) == 1.0) && tf_normalized2.getFloat(0,i,j,0) == 1.0) {
            //条件を満たせばi行j列とi-1行j列のノートナンバーを結合のする処理。
            e.setTiedFromPrevious(true)

          } else {
            // 結合しなればj番目のノートナンバーをsetEvidence。
            e.setEvidence(j)
          }
          break
        }
      }

    }
  //   if (note_num == "rest") {
  //     e.setRest(true)
  //   } else {
  //     e.setRest(false)
  //     // println("previous: ${note_num_list[i-1]}")
  //     // println("next: ${note_num}")

  //     if (note_num >= cfg.TF_NOTE_CON_COL_START && (i>=1 && note_num instanceof Integer && note_num_list[i-1] instanceof Integer && note_num == note_num_list[i-1])) {
  //       e.setTiedFromPrevious(true)
  //       println("Tied: ${i}")
  //     }

  //   }
  // }
  // for (i in 0..lastTick) {
  //   def note_num = note_num_list[i]
  //   def e = mr.getMusicElement("melody", measure, i)
  //   // println("note_num: ${note_num}")
  //   // println("e befor set Evidence: ${e.getMostLikely()}")
  //   if (note_num instanceof Integer && !e.tiedFromPrevious()) {
  //     // e.setEvidence(note_num % cfg.TF_NUM_OF_MELODY_ELEMENT)
  //     e.setEvidence(note_num)
  //     println("note_num: ${note_num}")
  //   }
  //   // println("e after set Evidence: ${e.getMostLikely()}")
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

    long currentTime = System.nanoTime()
    if (//tick == cfg.DIVISION - 1 &&
        //lastUpdateMeasure != measure &&
	      currentTime - lastUpdateTime >= 1000 * 1000 * 150) {
      String logname=""

      mr.getMusicElement(OUTLINE_LAYER, measure, tick).resumeUpdate()
      lastUpdateMeasure = measure
      lastUpdateTime = currentTime
      
      //get OUTLINE_LAYER Elements and Model the data for it to be inserted into model.
      FloatNdArray tf_input = preprocessing(measure)
      // FloatNdArray tf_input = inputCSV(cfg.INPUT_FILE_PATH)

      if (cfg.DEBUG) {
        logname="./log/" + "tf_input" + "measure${measure}" + ".csv"
        exportCVS(tf_input, logname)
      }
      FloatNdArray tf_output = predict(tf_input)

      if (cfg.DEBUG) {
        logname="./log/" + "tf_output" + "measure${measure}" + ".csv"
        exportCVS(tf_output, logname)
      }

      FloatNdArray normalized_data= normalize(tf_output)

      if (cfg.DEBUG) {
        logname="./log/" + "tf_normalized" + "measure${measure}" + ".csv"
        exportCVS(normalized_data, logname)
      }


      setEvidences(measure, tick, normalized_data)

    }
  }


  def automaticUpdate() {
    false
  }
  
}