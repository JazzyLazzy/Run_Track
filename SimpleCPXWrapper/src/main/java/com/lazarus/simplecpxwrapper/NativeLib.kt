package com.lazarus.simplecpxwrapper

class NativeLib {

    /**
     * A native method that is implemented by the 'simplecpxwrapper' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String
    external fun wtb(s:String)

    companion object {
        // Used to load the 'simplecpxwrapper' library on application startup.
        init {
            //System.loadLibrary("t")
            System.loadLibrary("simplecpxwrapper")
        }
    }
}