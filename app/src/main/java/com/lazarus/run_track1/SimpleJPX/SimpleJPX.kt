package com.lazarus.run_track1.SimpleJPX

import com.lazarus.run_track1.CPXGeoPoint

class SimpleJPX {

    companion object {
        init {
           // System.loadLibrary("SimpleCPXParser");
            System.loadLibrary("SimpleCPXWrapper")
        }
    }

    //external fun parseGPX(file: String): CPXGeoPoint?
    external fun wtb(file: String);
}