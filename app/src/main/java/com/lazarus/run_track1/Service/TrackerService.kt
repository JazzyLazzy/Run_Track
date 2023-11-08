package com.lazarus.run_track1.Service

import SimpleGPX.*
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.lazarus.run_track1.MainActivity
import com.lazarus.run_track1.MapsFragment.BROADCAST_ACTION
import com.lazarus.run_track1.MessageValues
import com.lazarus.run_track1.ParcelableTrackPoint
import com.lazarus.run_track1.createParcelableGPX
import java.io.File

class TrackerService : Service() {

    private lateinit var serviceLooper: Looper;
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient;
    private lateinit var locationRequest: LocationRequest;
    private lateinit var locationHeap: ArrayList<TrackPoint>;
    private lateinit var TSEL: TrackerServiceEndListener;
    private lateinit var servicer: Messenger;
    var mClients:ArrayList<Messenger> = ArrayList();
    private var GPS_TRACKING = false
    private val FOREGROUND_ID = 1337
    private var mMessenger:Messenger? = null;
    private var mBoundTrackerService:BoundTrackerService? = null;

    companion object {
        private var instance: TrackerService? = null

        fun getInstance(): TrackerService? {
            return instance
        }
    }

    inner class TrackerBinder: Binder() {
        fun getService():TrackerService = this@TrackerService;
    }

    inner class IncomingHandler(
        context: Context,
        private val applicationContext: Context = context.applicationContext
    ) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MessageValues.MSG_REGISTER_CLIENT.value -> {
                    Log.d("reg","client");
                    mClients.add(msg.replyTo);
                    super.handleMessage(msg);
                }
                MessageValues.MSG_UNREGISTER_CLIENT.value -> {
                    Log.d("reg","unreg")
                    mClients.remove(msg.replyTo);
                    super.handleMessage(msg);
                }
                MessageValues.MSG_START_TRACKING.value -> {
                    Log.d("service", "start tracking")
                    GPS_TRACKING = true
                    super.handleMessage(msg)
                }
                MessageValues.MSG_STOP_TRACKING.value -> {
                    Log.d("service", "stop tracking")
                    GPS_TRACKING = false
                    super.handleMessage(msg)
                }
                MessageValues.MSG_REÇEVOIR_TRKPT.value -> {
                    super.handleMessage(msg)
                }
                MessageValues.MSG_GET_GPX.value -> {
                    super.handleMessage(msg)
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    override fun onCreate() {
        super.onCreate();

        writeGPSTrack();
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this.applicationContext)
        locationRequest = LocationRequest.create()
        val rfile = File(this.application.filesDir.toString() + "track_rate.txt")
        if (rfile.exists()){
            locationRequest.interval = (rfile.readText().toDouble() * 1000).toLong()
            locationRequest.fastestInterval = (rfile.readText().toDouble() * 1000).toLong()
        }else{
            rfile.createNewFile()
            rfile.writeText("0.5");
            locationRequest.interval = 500
            locationRequest.fastestInterval = 500
        }
        locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY;
        startLocationUpdates();
        Log.d("track:", "Started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId);
        createNotificationChannel()
        Log.d("trackser:", "started");
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this.applicationContext, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this.applicationContext, "ChannelId1")
            .setContentTitle("GPSTrack")
            .setContentText("Watching you hehe watch out")
            .setContentIntent(pendingIntent)
            .build()

        startForeground(FOREGROUND_ID, notification)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return TrackerBinder();
    }

    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()
        val intent = Intent()
        intent.action = BROADCAST_ACTION;
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        val gpx = heapToGPX();
        val random = (0..100000).random()
        //val fileName = this.application.filesDir.toString() + "/tracks/" + random.toString() + ".gpx"
        //val simpleGPXWriter = SimpleGPXWriter("file://$fileName");
        //simpleGPXWriter.connectGPX(gpx)
        //simpleGPXWriter.writeGPX()
        intent.putExtra("trackpoints", createParcelableGPX(gpx))
        Log.d("trackser:", "koed");
        sendBroadcast(intent)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun sendLocationData():GPX{
        return heapToGPX();
    }

    private fun heapToGPX(): GPX {
        val tracks = ArrayList<trk>()
        tracks.add(createNewTrack(locationHeap))
        return GPX("run_track", "1.1", tracks);
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(){
        Log.d("servicelocation:","Started")
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult ?: return;
            locationResult.locations
            Log.d("servicelocation:", locationResult.lastLocation.toString())
            val location = locationResult.lastLocation
            val trkPoint = TrackPoint(
                location!!.latitude,
                location.longitude,
                location.altitude,
                GPXParserLocation.long_to_time(
                    location.time
                )
            )
            locationHeap.add(trkPoint);
            sendTrkPt(trkPoint);
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "ChannelId1", "GPSTracker", NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.lightColor = Color.RED
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val notificationManager =
                (applicationContext.getSystemService<NotificationManager>(
                    NotificationManager::class.java
                ) as NotificationManager)
            notificationManager.createNotificationChannel(notificationChannel)
            Log.d("serivec", "testing")
        } else {
            //TODO: Make compatible with Android 7-
        }
    }

    private fun writeGPSTrack() {
        val newLocationHeap = ArrayList<TrackPoint>()
        locationHeap = newLocationHeap;
    }

    private fun sendTrkPt(trackPoint: TrackPoint){
        val bundle = Bundle();
        val parcelableTrackPoint = ParcelableTrackPoint(
                trackPoint.latitude,
                trackPoint.longitude,
                trackPoint.elevation,
                trackPoint.time);
        bundle.putParcelable("track_point", parcelableTrackPoint);
        //val msg = Message.obtain(null, MessageValues.MSG_REÇEVOIR_TRKPT.value);
        //msg.data = bundle;
        //mClients[0].send(msg);
        val updateIntent = Intent("LOCATION_UPDATE");
        updateIntent.putExtra("data point", bundle);
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(updateIntent);
    }
}