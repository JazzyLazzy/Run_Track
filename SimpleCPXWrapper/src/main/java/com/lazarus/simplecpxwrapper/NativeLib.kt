package com.lazarus.simplecpxwrapper

import java.util.LinkedList

class NativeLib {

    /**
     * A native method that is implemented by the 'simplecpxwrapper' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String
    external fun wtb(s:String)
    external fun parseGPX(s:String):CPXGeoPoint?

    companion object {
        // Used to load the 'simplecpxwrapper' library on application startup.
        init {
            //System.loadLibrary("t")
            System.loadLibrary("expat")
            System.loadLibrary("simplecpxwrapper")
        }
    }
}