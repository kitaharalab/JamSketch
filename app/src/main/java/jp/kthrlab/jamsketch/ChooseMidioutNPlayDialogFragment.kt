package jp.kthrlab.jamsketch

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.ArrayAdapter
import jp.crestmuse.cmx.processing.ChooseMidioutDialogFragment
import jp.crestmuse.cmx.processing.DeviceNotAvailableException
import jp.crestmuse.cmx.sound.SoundUtils
import jp.kshoji.javax.sound.midi.MidiUnavailableException

@SuppressLint("ValidFragment")
class ChooseMidioutNPlayDialogFragment(val callback: () -> Unit) : ChooseMidioutDialogFragment() {

//    fun setMethod(method:() -> Unit): ChooseMidioutAndPlayDialogFragment {
//        callback = method
//        return this
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        try {
            val midiOutDeviceInfo = SoundUtils.getMidiOutDeviceInfo()
            builder.setTitle("Select MIDI OUT Device...")
                    .setSingleChoiceItems(
                            ArrayAdapter<Any?>(activity as Context, layout, midiOutDeviceInfo as List<Any?>),
                            0
                    ) { dialog, which ->
                        cmx.setMidiOutDevice(midiOutDeviceInfo[which]!!.name)
                        callback()
                        dialog.dismiss()
                        cmx.playMusic()
                    }
        } catch (e: MidiUnavailableException) {
            throw DeviceNotAvailableException("MIDI device not available")
        }
        return builder.create()
    }
}