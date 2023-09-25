package com.lazarus.run_track1.Service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import com.lazarus.run_track1.MessageValues

class BoundTrackerService : Service() {

    private val binder = LocalBinder()
    private var callback: MyServiceCallback? = null

    inner class LocalBinder : Binder() {
        fun getService(): BoundTrackerService = this@BoundTrackerService
    }

    interface MyServiceCallback {
        fun onDataReceived(data: String)
    }

    fun setCallback(callback: MyServiceCallback?) {
        this.callback = callback
    }

    // Simulate sending data to the activity
    fun sendDataToActivity(data: String) {
        callback?.onDataReceived(data)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
}