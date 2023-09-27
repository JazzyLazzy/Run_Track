package com.lazarus.run_track1.SettingsFragment.LocationTrackRateFragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import com.lazarus.run_track1.R
import java.io.File

class LocationTrackRateFragment : Fragment() {

    private lateinit var trackRateBarValue: TextView
    private lateinit var trackRateBar: SeekBar
    private lateinit var fileName:String
    private var value:Double = 0.25

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fileName = requireActivity().filesDir.toString() + "track_rate.txt";
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.location_track_rate_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trackRateBarValue = view.findViewById(R.id.track_rate_bar_value)
        trackRateBar = view.findViewById(R.id.track_rate_bar)
        val rfile = File(fileName)
        if (rfile.exists()){
            value = rfile.readText().toDouble()
        }else{
            rfile.createNewFile()
            rfile.writeText(value.toString());
        }
        trackRateBarValue.text = "Value: $value s"
        trackRateBar.progress = (value / .25 - .25).toInt()

        trackRateBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                value = 0.25 + (progress * 0.25)
                trackRateBarValue.text = "Value: $value s"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val wfile = File(fileName);
                wfile.writeText(value.toString());
            }
        })
    }

}