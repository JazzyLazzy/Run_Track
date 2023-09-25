package com.lazarus.run_track1.HActivitiesFragment

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lazarus.run_track1.databinding.PlaqueABasBinding


class PlaqueÀBas : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val liaison = PlaqueABasBinding.inflate(inflater, container, false);
        return liaison.root;
    }

}

fun exempleNouveau():PlaqueÀBas {
    return PlaqueÀBas();
}