package jp.kthrlab.jamsketch

import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
import java.io.IOException
import java.nio.ByteBuffer

class TFLTestClient(private val assetManager: AssetManager) {
    private var tflite: Interpreter? = null

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
            val buffer: ByteBuffer = loadModelFile(assetManager, Config.TFL_MODEL_FILE)
            tflite = Interpreter(buffer)
//            tflite?.allocateTensors()

            if (BuildConfig.DEBUG) {
                println("TFLite model loaded.")
                printTensor(tflite!!)
            }

        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    @Synchronized
    fun recommend(input1: Int, input2: Int): Array<FloatArray>? {
        println("recommend(${input1}, ${input2})")
        val inputs =Array(1) {FloatArray(2)}

        inputs[0][0] = input1.toFloat()
        inputs[0][1] = input2.toFloat()

        val outputs = Array(1) {FloatArray(1)} //FloatArray(Config.OUTPUT_LENGTH)
//        val outputsMap: MutableMap<Int, Any> = HashMap()
//        outputsMap[0] = outputs
        println("Before: in = ${inputs[0][0]}, ${inputs[0][1]}, out = ${outputs[0][0]}}}")
        tflite!!.run(inputs, outputs)
//        tflite!!.runForMultipleInputsOutputs(inputs,         outputsMap[0] = outputs)
        println("After: in = ${inputs[0][0]}, ${inputs[0][1]}, out = ${outputs[0][0]}}")
        return outputs
    }

    @Synchronized
    fun recommend(input: Int?): List<Any>? {
        println("recommend(${input})")
        val inputs = Array(1) {FloatArray(1)}
        inputs[0][0] = input?.toFloat()!!

        // Run inference.
        val outputValues = Array(1) {FloatArray(1)} //FloatArray(Config.OUTPUT_LENGTH)
        val outputs: MutableMap<Int, Any> = HashMap()
        println("Before: in = ${inputs}, out = ${outputValues}")
        outputs[0] = outputValues
        tflite!!.runForMultipleInputsOutputs(inputs, outputs)
        println("After: in = ${inputs}, out = ${outputValues}")
        println(outputs[0])
//        tflite!!.run(inputs[0],outputs[0])
//        outputs.forEach{
//            println("${it.key }, ${it.value}")
//        }
        return outputs.toList()
//        return  outputs.//postprocess(outputIds, confidences, selectedMovies)
    }

    fun testTFL() {
        println("+++++ testTFL +++++")
        load()
        recommend(175, 80)
        recommend(120, 22)
        unload()
        println("----- testTFL -----")
    }

    private fun printTensor(tflite :Interpreter)
    {
        println("InputTensor count: ${tflite?.inputTensorCount}")
        for (i in 0 until tflite?.inputTensorCount!!) {
            println("InputTensor shape size: ${tflite?.getInputTensor(i)?.shape()!!.size}")
            for (j in 0 until tflite?.getInputTensor(i)!!.shape().size) {
                println("InputTensor shape[${j}]: ${tflite?.getInputTensor(i)?.shape()!![j]}")
            }
            println("InputTensor index: ${tflite?.getInputTensor(i)?.index()}")
            println("InputTensor name: ${tflite?.getInputTensor(i)?.name()}")
            println("InputTensor dataType: ${tflite?.getInputTensor(i)?.dataType()}")
        }
        println("OutputTensor count: ${tflite?.outputTensorCount}")
        for (i in 0 until tflite?.outputTensorCount!!) {
            println("OutputTensor shape size: ${tflite?.getOutputTensor(i)?.shape()!!.size}")
            for (j in 0 until tflite?.getOutputTensor(i)!!.shape().size) {
                println("OutputTensor shape[${j}]: ${tflite?.getOutputTensor(i)?.shape()!![j]}")
            }
            println("OutputTensor index: ${tflite?.getOutputTensor(i)?.index()}")
            println("OutputTensor name: ${tflite?.getOutputTensor(i)?.name()}")
            println("OutputTensor dataType: ${tflite?.getOutputTensor(i)?.dataType()}")
        }

    }

}