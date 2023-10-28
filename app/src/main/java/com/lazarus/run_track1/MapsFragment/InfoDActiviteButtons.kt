package com.lazarus.run_track1.MapsFragment

import SimpleGPX.SimpleGPXParser
import android.content.Context
import android.os.Bundle
import com.lazarus.run_track1.HActivitiesFragment.ActivityMapFragment
import com.lazarus.run_track1.HActivitiesFragment.HActivityFragment
import com.lazarus.run_track1.R

enum class InfoDActiviteButtons {
    enCliquéAdaptateur {
        override fun invokeAction(fragment:HActivityFragment, nomFicher: String) {
            val activityMapFragment = ActivityMapFragment();
            val fileName = fragment.context?.filesDir.toString() + "/tracks/$nomFicher"
            val fileNameBundle = Bundle().apply {
                putString("gpx_file", fileName)
            }
            val bogusGPXParser = SimpleGPXParser("file://$fileName")
            val newTag = "track"

            activityMapFragment.arguments = fileNameBundle

            fragment.requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, activityMapFragment, newTag)
                .addToBackStack(null)
                .commit()
        }
    };
    abstract fun invokeAction(fragment: HActivityFragment, nomFicher:String);
}