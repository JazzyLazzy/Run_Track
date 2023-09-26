package com.lazarus.run_track1.NativeJNI

class MonJNI {
    companion object  {
        init {
            System.loadLibrary("run_track1")
        }
    }

    external fun helloWorld(): String
}