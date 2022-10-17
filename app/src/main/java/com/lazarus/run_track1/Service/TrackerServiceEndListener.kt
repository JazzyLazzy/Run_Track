package com.lazarus.run_track1.Service

import com.lazarus.bogusgpxparser.TrackPoint

interface TrackerServiceEndListener {
    fun onTrackerServiceEnd(locationHeap:ArrayList<TrackPoint>)
}