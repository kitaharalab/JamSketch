package jp.kthrlab.jamsketch;

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment

//import android.support.v4.app.DialogFragment
//import android.support.v7.app.AlertDialog

@SuppressLint("ValidFragment")
class OpenGooglePlayDialogFragment(): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
            builder.setTitle("Install Synthesizer...")
                    .setMessage(getString(R.string.synthesizer_message, getString(R.string.synthesizer_name)))
                    .setPositiveButton( R.string.ok , DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.playstore_url) + getString(R.string.synthesizer_package))))
                    })
                    .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })

//                    .setSingleChoiceItems(
//                            ArrayAdapter<Any?>(activity, layout, midiOutDeviceInfo),
//                            0
//                    ) { dialog, which ->
//                        cmx.setMidiOutDevice(midiOutDeviceInfo[which]!!.name)
//                        dialog.dismiss()
//                    }
        return builder.create()
    }
}