package com.lazarus.run_track1.MapsFragment

import android.content.Context
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.lazarus.run_track1.R

class VelocityView(context: Context) : AppCompatTextView(context){

}

class DurationView(context: Context) : AppCompatTextView(context){

}

class DistanceView(context: Context) : AppCompatTextView(context){

}

class StatsLayout(context: Context) : ConstraintLayout(context) {
    private val velocityView = VelocityView(context);
    private val durationView = DurationView(context);
    private val distanceView = DistanceView(context);

    init {
        velocityView.setText("0");
        durationView.setText("0");
        distanceView.setText("0");
        this.addView(velocityView)
        this.addView(durationView)
        this.addView(distanceView)
        this.id = View.generateViewId();
        this.visibility = GONE;
    }
}

fun set_stats_layout_constraints(statsLayout:StatsLayout) {
    val constraintSet = ConstraintSet();
    val parentLayout = statsLayout.parent as ConstraintLayout;
    constraintSet.clone(parentLayout);
    constraintSet.connect(statsLayout.id, ConstraintSet.BOTTOM, R.id.main_map_view, ConstraintSet.BOTTOM);
    constraintSet.applyTo(parentLayout);
}
