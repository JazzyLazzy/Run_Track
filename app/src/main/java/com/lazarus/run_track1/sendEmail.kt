package com.lazarus.run_track1

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity


fun sendEmail(subject:String, contents: Uri, context:Context?){
    val i = Intent(Intent.ACTION_SEND)
    i.type = "message/rfc822"
    i.putExtra(Intent.EXTRA_EMAIL, arrayOf("lazarusjturpaud@protonmail.com"))
    i.putExtra(Intent.EXTRA_SUBJECT, subject)
    i.putExtra(Intent.EXTRA_STREAM, contents)
    try {
        context!!.startActivity(Intent.createChooser(i, "Send mail..."))
    } catch (ex: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "There are no email clients installed.",
            Toast.LENGTH_SHORT
        ).show()
    }
}