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
import java.io.BufferedReader
import java.io.InputStreamReader

class LicenseDialogue : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{
            val builder = AlertDialog.Builder(it)
            val licence = readAssetFile("licence.txt");
            val crédits = readAssetFile("crédits.txt");
            val inflater = requireActivity().layoutInflater;
            val builderView = inflater.inflate(R.layout.license_dialogue, null);
            builder.setView(builderView);
            builder.setTitle("Licenses and Credits here:");
            builder.setMessage(licence + crédits);
            builder.setPositiveButton("OK", null);
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Intent(requireActivity(), LicenseDialogueService::class.java).also { intent ->
            requireActivity().startService(intent)
        }
    }

    private fun readAssetFile(fileName: String): String {
        val assetManager = this.requireActivity().assets;
        val inputStream = assetManager.open(fileName)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        return bufferedReader.use { it.readText() }
    }
}
