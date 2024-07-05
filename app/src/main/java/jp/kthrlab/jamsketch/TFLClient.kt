package jp.kthrlab.jamsketch

import android.app.Activity
import org.tensorflow.lite.Interpreter
import java.io.IOException
import java.nio.ByteBuffer

class TFLClient(private val activity: Activity) {
    private var tflite: Interpreter? = null

    init {
//        this.load()
    }

    fun isLoaded(): Boolean {
        return tflite != null
    }

    fun load() {
        loadModel()
    }

    @Synchronized
    fun unload() {
        tflite!!.close()
    }

    /** Load TF Lite model. */
    private fun loadModel() {
        try {
            val buffer: ByteBuffer = loadModelFile(activity.assets, Config.TFL_MODEL_FILE)
            tflite = Interpreter(buffer)

            if (BuildConfig.DEBUG) {
                println("TFLite model loaded.")
                printTensor(tflite!!)
            }

        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    @Synchronized
    fun predict() {
        val inputTensor = tflite!!.getInputTensor(0)
        val outputTensor = tflite!!.getOutputTensor(0)

        println(inputTensor.shape().size)
        println(outputTensor.shape().size)

        val inputs =
            Array(inputTensor.shape()[0]){
                Array(inputTensor.shape()[1]){
                    Array(inputTensor.shape()[2]){
                        FloatArray(inputTensor.shape()[3])
                    }
                }
            }

        val outputs =
            Array(outputTensor.shape()[0]){
                Array(outputTensor.shape()[1]){
                    Array(outputTensor.shape()[2]){
                        FloatArray(outputTensor.shape()[3])
                    }

                }
            }

        println(inputs)
        println(outputs)
        activity.runOnUiThread {
            tflite!!.run(inputs, outputs)
        }
        println("outputs = $outputs")
    }

    @Synchronized
    fun predict(input: Array<Array<Array<FloatArray>>>): Array<Array<Array<FloatArray>>> {
        if (BuildConfig.DEBUG) println("--------- beginning of TFLClient::predict")
        val outputTensor = tflite!!.getOutputTensor(0)
        var output =
            Array(outputTensor.shape()[0]){
                Array(outputTensor.shape()[1]){
                    Array(outputTensor.shape()[2]){
                        FloatArray(outputTensor.shape()[3])
                    }
                }
            }

        activity.runOnUiThread {
            tflite!!.run(input, output)
        }
        if (BuildConfig.DEBUG) println("--------- end of TFLClient::predict")

        return output
    }

//    @Synchronized
//    fun predict(input: FloatArray): Array<FloatArray>? {
//        println("recommend(${input}")
//        val inputs =Array(1) {FloatArray(4)}
//        inputs[0][0] = input[0]
//        inputs[0][1] = input[1]
//        inputs[0][3] = input[2]
//        inputs[0][4] = input[3]
//
//        val outputs = Array(1) {FloatArray(4)} //FloatArray(Config.OUTPUT_LENGTH)
////        val outputsMap: MutableMap<Int, Any> = HashMap()
////        outputsMap[0] = outputs
////        println("Before: in = ${inputs[0][0]}, ${inputs[0][1]}, out = ${outputs[0][2]}}}")
//        tflite!!.run(inputs, outputs)
////        tflite!!.runForMultipleInputsOutputs(inputs,         outputsMap[0] = outputs)
//        println("After: in = ${inputs[0][0]}, ${inputs[0][1]}, out = ${outputs[0][0]}, ${outputs[0][1]}, ${outputs[0][3]}, ${outputs[0][4]}")
//        return outputs
//    }
//
//    @Synchronized
//    fun predict(input: Int?): List<Any>? {
//        println("recommend(${input})")
//        val inputs = Array(1) {FloatArray(1)}
//        inputs[0][0] = input?.toFloat()!!
//
//        // Run inference.
//        val outputValues = Array(1) {FloatArray(1)} //FloatArray(Config.OUTPUT_LENGTH)
//        val outputs: MutableMap<Int, Any> = HashMap()
//        println("Before: in = ${inputs}, out = ${outputValues}")
//        outputs[0] = outputValues
//        tflite!!.runForMultipleInputsOutputs(inputs, outputs)
//        println("After: in = ${inputs}, out = ${outputValues}")
//        println(outputs[0])
////        tflite!!.run(inputs[0],outputs[0])
////        outputs.forEach{
////            println("${it.key }, ${it.value}")
////        }
//        return outputs.toList()
////        return  outputs.//postprocess(outputIds, confidences, selectedMovies)
//    }

    fun testTFL() {
        println("+++++ testTFL +++++")
        if (!isLoaded()) load()
        predict()
//        unload()
        println("----- testTFL -----")
    }

    private fun printTensor(tflite :Interpreter)
    {
        println("+++ printTensor +++")
        println("InputTensor count: ${tflite.inputTensorCount}")
        for (i in 0 until tflite.inputTensorCount) {
            println("InputTensor shape size: ${tflite.getInputTensor(i)?.shape()!!.size}")
            for (j in 0 until tflite.getInputTensor(i)!!.shape().size) {
                println("InputTensor shape[${j}]: ${tflite.getInputTensor(i)?.shape()!![j]}")
            }
            println("InputTensor index: ${tflite.getInputTensor(i)?.index()}")
            println("InputTensor name: ${tflite.getInputTensor(i)?.name()}")
            println("InputTensor dataType: ${tflite.getInputTensor(i)?.dataType()}")
        }
        println("OutputTensor count: ${tflite.outputTensorCount}")
        for (i in 0 until tflite.outputTensorCount) {
            println("OutputTensor shape size: ${tflite.getOutputTensor(i)?.shape()!!.size}")
            for (j in 0 until tflite.getOutputTensor(i)!!.shape().size) {
                println("OutputTensor shape[${j}]: ${tflite.getOutputTensor(i)?.shape()!![j]}")
            }
            println("OutputTensor index: ${tflite.getOutputTensor(i)?.index()}")
            println("OutputTensor name: ${tflite.getOutputTensor(i)?.name()}")
            println("OutputTensor dataType: ${tflite.getOutputTensor(i)?.dataType()}")
        }
        println("--- printTensor ---")
    }

}