package jp.kthrlab.jamsketch.view.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jp.crestmuse.cmx.filewrappers.SCCDataSet
import jp.crestmuse.cmx.processing.gui.SimplePianoRoll
import jp.kthrlab.jamsketch.music.data.MusicData
import processing.core.PApplet
import java.io.File
import java.util.*

fun makeLog(action: String, musicData: MusicData, logDir: String, pApplet: PApplet) {
    val logname = "output_" + (Date()).toString().replace(" ", "_").replace(":", "-")
    if (action == "melody") {
        val midname: String = logDir + "/" + logname + "_melody.mid"
        (musicData.scc as SCCDataSet).toWrapper().toMIDIXML().writefileAsSMF(midname)
        SimplePianoRoll.println("saved as $midname")

        val sccname: String = logDir + "/" + logname + "_melody.sccxml"
        (musicData.scc as SCCDataSet).toWrapper().writefile(sccname)
        SimplePianoRoll.println("saved as $sccname")

        val jsonname: String = logDir + "/" + logname + "_curve.json"
        musicData.channelCurveSet.forEach { channelCurve ->
            pApplet.saveStrings(
                jsonname,
                arrayOf(jacksonObjectMapper().writeValueAsString(channelCurve.second))
            )
        }
        SimplePianoRoll.println("saved as $jsonname")
        val pngname: String = logDir + "/" + logname + "_screenshot.png"
        pApplet.save(pngname)
        SimplePianoRoll.println("saved as $pngname")

        // for debug
        File("${logDir.plus(File.separator).plus(logname)}_noteList.txt").writeText(
            (musicData.scc as SCCDataSet).getFirstPartWithChannel(1).noteList.toString()
        )
        //      new File("${CFG.LOG_DIR}/${logname}_noteOnlyList.txt").text = (melodyData.scc as SCCDataSet).getFirstPartWithChannel(1).getNoteOnlyList().toString()
    } else {
        val txtname: String = logDir.toString() + "/" + logname + "_" + action + ".txt"
        pApplet.saveStrings(txtname, arrayOf(action))
        SimplePianoRoll.println("saved as $txtname")
    }
}