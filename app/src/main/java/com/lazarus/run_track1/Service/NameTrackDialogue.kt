package com.lazarus.run_track1.Service

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.lazarus.run_track1.R
import java.time.LocalDateTime

class NameTrackDialogue : DialogFragment() {

    var trackName:String? = null
    private lateinit var listener: DialogInfoReceivedListener


    interface DialogInfoReceivedListener {
        fun onDialogPositiveClick(dialog: DialogFragment, trackName:String?)
        fun onDestroy(localDateTime: LocalDateTime?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Intent(requireActivity(), NameTrackDialogueService::class.java).also { intent ->
            requireActivity().startService(intent)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = this.parentFragment as DialogInfoReceivedListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val builderView = inflater.inflate(R.layout.name_track_dialogue, null)
            builder.setView(builderView)
            builder.setMessage("Name Your Activity")
                .setPositiveButton("Name",
                    DialogInterface.OnClickListener { dialog, id ->
                        val textVal: EditText = builderView.findViewById<View>(R.id.track_name) as EditText
                        trackName = textVal.text.toString()
                        Log.d("holdon", trackName!!)
                        listener.onDialogPositiveClick(this, trackName)
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        val localDateTime = LocalDateTime.now()
        listener.onDestroy(localDateTime)
    }
}