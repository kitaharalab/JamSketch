package jp.kthrlab.jamsketch

import android.os.Handler
import android.os.HandlerThread
import jp.crestmuse.cmx.inference.MusicRepresentation
import jp.crestmuse.cmx.misc.ChordSymbol2
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer

class TF1Worker {
    private var lastUpdateMeasure = -1
    private var lastUpdateTime = -1L
    private val CHORD_VECTORS = mapOf(
        "C" to floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f),
        "F" to floatArrayOf(1f, 0f, 0f, 1f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 0f),
        "G" to floatArrayOf(0f, 0f, 1f, 0f, 0f, 1f, 0f, 1f, 0f, 0f, 0f, 1f)
    )
    private var byteBuffer: ByteBuffer
    private var interpreter: Interpreter

    private var handlerThread: HandlerThread = HandlerThread("tf1Worker_thread")
    private var handler: Handler

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        byteBuffer = loadModelFile(JamSketchActivity.jamSketchAssets, Config.TFL_MODEL_FILE)
        interpreter = Interpreter(byteBuffer)
    }

    fun updated(measure: Int, tick: Int, layer: String?, mr: MusicRepresentation?) {

        val th = Thread {
//            if (BuildConfig.DEBUG) println("+++++++++ beginning of TF1NoteSeqWorker::updated(${measure}, ${tick}, ${layer}, ${mr})")
            synchronized(interpreter) {
                val outputTensor = interpreter.getOutputTensor(0)
                val outputShape = outputTensor.shape()
                var tf_output =
                    Array(outputShape[0]){
                        Array(outputShape[1]){
                            Array(outputShape[2]){
                                FloatArray(outputShape[3])
                            }
                        }
                    }

                val currentTime = System.nanoTime()
                if (currentTime - lastUpdateTime >= 1000 * 1000 * Config.MELODY_EXECUTION_SPAN.toLong()) {

                    mr!!.getMusicElement("curve", measure, tick).resumeUpdate()

                    lastUpdateMeasure = measure
                    lastUpdateTime = currentTime

                    val tf_input = preprocessing(measure, mr)
                    interpreter.run(tf_input, tf_output)
                    var normalized_data : Array<Array<Array<FloatArray>>>
                    normalized_data = normalize(tf_output)
                    setEvidences(measure, tick, normalized_data, mr)
                }
            }


        }
        handler.post(th)
        if (BuildConfig.DEBUG) println("_________________tf1 thread started")
    }

    //preprocessing input data
    private fun preprocessing(measure: Int, mr: MusicRepresentation): Array<Array<Array<FloatArray>>> {
        val nn_from = Config.TF_NOTE_NUM_START
        val tf_row = Config.getDivision()
        val tf_column = Config.TF_MODEL_INPUT_COL

        val tf_input =
            Array(1){
                Array(tf_row){
                    Array(tf_column){
                        FloatArray(1)
                    }
                }
            }

        val mes = mr.getMusicElementList("curve")
        val me_start = (mes.size / Config.NUM_OF_MEASURES) * measure
        val me_end = me_start + Config.getDivision()
        val me_per_measure = mes.subList(me_start, me_end)

        for (i in 0 until tf_row) {
            val note_num_f : Double = me_per_measure[i].mostLikely as Double
            print("$note_num_f, ")
            if (!note_num_f.isNaN()) {
                val note_number = Math.floor(note_num_f) - nn_from
                tf_input[0][i][note_number.toInt()][0] = 1.0f
            } else {
                tf_input[0][i][Config.TF_REST_COL][0] = 1.0f
            }

            //set chord
            val chord_column_from = Config.TF_CHORD_COL_START
            val chord = mr.getMusicElement("chord", measure, i).mostLikely as ChordSymbol2//getChord(measure, i)
            val chord_vec = CHORD_VECTORS[chord.toString()]

            for (j in 0 until Config.TF_NUM_OF_MELODY_ELEMENT) {
                val chord_value = chord_vec!![j]
                tf_input[0][i][chord_column_from + j][0] = chord_value
            }
        }
        println()
        return tf_input
    }

    //Normalize the prediction data which if it is under 0.5, be 0, if it is over 0.5, be 1.
    private fun normalize(tf_output: Array<Array<Array<FloatArray>>>): Array<Array<Array<FloatArray>>> {
        val tf_row = tf_output[0].size//Config.getDivision()
        val tf_column = tf_output[0][0].size
        val size3 = (tf_column - 1) / 2

        // Create a 16x60 matrix.
        val tmp_output =
            Array(1){
                Array(tf_row){
                    Array(size3){
                        FloatArray(1)
                    }
                }
            }

        // Assign the elements from columns 60 to 120 (inclusive) of all rows in tf_output to tmp_output.
        for (i in 0 until tf_row) {
            for (j in size3 until tf_column - 1) {
                tmp_output[0][i][j - size3][0] =  tf_output[0][i][j][0]
            }
        }

        // Handle the processing of the function (make_note_msgs) in 24measure_MelodyGenerationDemo. ipynb.
        for (i in 0 until tf_row) {
            for (j in 0 until size3) {
//                println("tf_output[0][${i}][${j}][0] = ${tf_output[0][i][j][0]}")
//                println("tmp_output[0][${i}][${j}][0] = ${tmp_output[0][i][j][0]}")
                if (tf_output[0][i][j][0] <= 0.5
                    && tmp_output[0][i][j][0] > 0.5)
                {
                    if (i >= 1 && tmp_output[0][i - 1][j][0] < 0.5 && tf_output[0][i - 1][j][0] < 0.5) {
                        tf_output[0][i][j][0] = tmp_output[0][i][j][0] + tf_output[0][i][j][0]
                        tf_output[0][i][j + size3][0] = 0.0f
                    }
                }
            }
        }

        // For each value in tf_output, replace it with 1.0 if it's greater than or equal to 0.5, and 0 otherwise.
        for (i in 0 until tf_row) {
            for (j in 0 until tf_column) {
                if (tf_output[0][i][j][0] >= 0.5) {
                    tf_output[0][i][j][0] = 1.0f
                } else {
                    tf_output[0][i][j][0] = 0.0f
                }
            }
        }
        return tf_output
    }

    private fun setEvidences(measure: Int, lastTick: Int, tf_normalized: Array<Array<Array<FloatArray>>>, mr: MusicRepresentation) {
//        if(BuildConfig.DEBUG) println("+++++++++ beginning of TF1NoteSeqWorker::setEvidences(${measure}, ${lastTick}, ${tf_normalized.contentDeepToString()})")

        val tf_row = Config.getDivision()
        val tf_column = Config.TF_MODEL_OUTPUT_COL
        val tf_normalized2 =
            Array(1){
                Array(tf_row){
                    Array((tf_column - 1) / 2){
                        FloatArray(1)
                    }
                }
            }

        for (i in 0 until tf_row) {
            for (j in 60 until tf_column - 1) {
                tf_normalized2[0][i][j - 60][0] = tf_normalized[0][i][j][0]
            }
        }

        for (i in 0..lastTick) {
            val e = mr.getMusicElement("melody", measure, i)

            if (tf_normalized[0][i][120][0] == 1.0f) {
                e.setRest(true)
            } else {
                e.setRest(false)

                if (i >= 1) {
                    for (j in 0 until (tf_column - 1) / 2) {
                        if ((tf_normalized2[0][i - 1][j][0] == 1.0f || tf_normalized[0][i - 1][j][0] == 1.0f) && tf_normalized2[0][i][j][0] == 1.0f) {
                            e.setTiedFromPrevious(true)
                        } else if (tf_normalized[0][i][j][0] == 1.0f) {
                            e.setEvidence(j)
                        }
                    }
                }
            }
        }
    }

}