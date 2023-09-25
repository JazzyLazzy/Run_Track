package com.lazarus.run_track1.SettingsFragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.lazarus.run_track1.R
import com.lazarus.run_track1.Service.NameTrackDialogueService

class LicenseDialogue : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val builderView = inflater.inflate(R.layout.name_track_dialogue, null);
            builder.setView(builderView);
            builder.setMessage("Licenses here:");
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Intent(requireActivity(), LicenseDialogueService::class.java).also { intent ->
            requireActivity().startService(intent)
        }
    }
}