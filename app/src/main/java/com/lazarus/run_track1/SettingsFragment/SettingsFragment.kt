package com.lazarus.run_track1.SettingsFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lazarus.run_track1.MainActivity
import com.lazarus.run_track1.R
import com.lazarus.run_track1.SettingsFragment.LocationTrackRateFragment.LocationTrackRateFragment
import com.lazarus.run_track1.databinding.SettingsFragmentBinding

class SettingsFragment : Fragment() {

    private var mtag: String? = null
    private var scale = 0f
    private var pixels = 0;
    private lateinit var binding: SettingsFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mtag = tag
        scale = requireContext().resources!!.displayMetrics.density
        pixels = (100 * scale + 0.5f).toInt()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = SettingsFragmentBinding.inflate(inflater);
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState);
        binding.licenseButton.setOnClickListener {
            val licenseDialogue = LicenseDialogue();
            licenseDialogue.show(childFragmentManager, licenseDialogue.tag);
        }
        binding.locationTrackRateButton.setOnClickListener {
            val locationTrackRateFragment = LocationTrackRateFragment();
            this.requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, locationTrackRateFragment, "LocationTrackRate")
                .commit()
        }
    }

}