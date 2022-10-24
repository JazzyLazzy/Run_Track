package com.lazarus.run_track1.MapsFragment

import SimpleGPX.*

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.*
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.lazarus.run_track1.*

import com.lazarus.run_track1.Service.NameTrackDialogue
import com.lazarus.run_track1.Service.NameTrackDialogueService
import com.lazarus.run_track1.Service.TrackerService
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.Consumer
import kotlin.io.path.deleteIfExists


const val BROADCAST_ACTION = "com.lazarus.run_track1.MapFragment.MAPSFRAGMENT.TRACKRECEIVER";

class MapFragment : Fragment(), NameTrackDialogue.DialogInfoReceivedListener {

    private lateinit var mMapView: MapView;
    private lateinit var mRotationGestureOverlay:RotationGestureOverlay;
    private lateinit var bogusCopyrightOverlay:CopyrightOverlay;
    private lateinit var mLocationOverlay:MyLocationNewOverlay;
    private lateinit var startButton: Button;
    private lateinit var addWaypointButton: Button;
    private lateinit var endButton: Button;
    private lateinit var pauseButton: Button;
    private lateinit var unpauseButton: Button;
    private lateinit var bLl:LinearLayout;
    private lateinit var parentLayout: ConstraintLayout;
    private lateinit var fileNames:ArrayList<String>;
    private var activities = ArrayList<Button>();
    private var mtag: String? = null
    private var scale = 0f
    private var pixels = 0
    //private lateinit var TrackReceiver: BroadcastReceiver;
    private lateinit var gpx:GPX;
    private var myTrack:Polyline? = null;
    private var gpxFile: SimpleGPXFile = SimpleGPXFile(this.context?.filesDir.toString() + "/tracks/temp1.gpx")
    private var tracking:Boolean = false;
    private var isBound:Boolean? = null;
    private var mService:Messenger? = null;
    private lateinit var mMessenger:Messenger;

    private var connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.d("track:","connected");
            mService = Messenger(iBinder);
            try{
                val msg = Message.obtain(null, MessageValues.MSG_REGISTER_CLIENT.value);
                msg.replyTo = mMessenger;
                mService?.send(msg);
            }catch (err:RemoteException){

            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
           /* try{
                val msg = Message.obtain(null, MessageValues.MSG_UNREGISTER_CLIENT.value);
                msg.replyTo = mMessenger;
                mService?.send(msg);
            }catch(err:RemoteException){

            }*/
            mService = null;
        }
    }

    private val TrackReceiver:BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val trkpts = ArrayList<TrackPoint>()
            val fileName = intent!!.getStringExtra("trackpoints")
            Log.d("bundle",fileName!!)
            val readGPX = SimpleGPXParser("$fileName")
            //Log.d("bundle", Stringify(fileName))
            val tracks = readGPX.parseGPX().tracks
            readGPX.deleteGPXFile();
            Log.d("bundle", tracks[0].name!!)
            gpx.addTracks(tracks)
            /*val ptp:ArrayList<ParcelableTrackPoint> =
                intent!!.getParcelableArrayListExtra<ParcelableTrackPoint>("trackpoints")
                        as ArrayList<ParcelableTrackPoint>
            Log.d("broadcast", "broad")
            ptp.forEach(Consumer { parcelableTrackPoint: ParcelableTrackPoint ->
                val trackPoint = TrackPoint(
                    parcelableTrackPoint.latitude,
                    parcelableTrackPoint.longitude,
                    parcelableTrackPoint.elevation,
                    parcelableTrackPoint.time
                )
                trkpts.add(trackPoint)
            })
            val trksegs = ArrayList<trkseg>()
            val trkseg = trkseg(trkpts)
            trksegs.add(trkseg)
            val trk = trk("trk_1", trksegs)
            gpx.addTrack(trk);*/
            /*val date = Date(System.currentTimeMillis());
            val timeStamp: String = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Date())
            writeToGPX(timeStamp);*/
        }
    }

    /*@SuppressLint("HandlerLeak")
    inner class IncomingHandler(
        context: Context,
        private val applicationContext: Context = context.applicationContext
    ) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MessageValues.MSG_GET_TRKPT.value -> {
                    val trkPoint = msg.data.getParcelable<ParcelableTrackPoint>("track_point") as TrackPoint;
                    gpx.addTrackPointToEnd(trkPoint);
                    val geoPoint = GeoPoint(trkPoint.latitude, trkPoint.longitude, trkPoint.elevation);
                    if (myTrack != null){
                        myTrack!!.addPoint(geoPoint);
                    }else{
                        myTrack = Polyline(mMapView);
                        mMapView.overlays.add(myTrack);
                        myTrack!!.addPoint(geoPoint);
                    }
                }
            }
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("fragment","created");
        //mMessenger = Messenger(IncomingHandler(requireContext()));
        if(savedInstanceState != null){
            val saveGPX = savedInstanceState.getParcelable<ParcelableGPX>("Saved_GPX")
            if (saveGPX != null) {
                gpx = createGPXFromParcelable(saveGPX)
                Log.d("fragment", "gpx restored")
            }
            /*requireActivity().bindService(Intent(activity, TrackerService::class.java), connection, Context.BIND_AUTO_CREATE);
            val saveService = savedInstanceState.getParcelable<Messenger>("Service");
            if (saveService != null){
                mService = saveService;
                if (mService != null) {
                    mService!!.send(Message.obtain(null, MessageValues.MSG_REGISTER_CLIENT.value));
                }
            }*/
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Create parent and map layout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                i_really_just_do_not_know_how_to_name_this_function_and_i_am_just_too_tired_to_care_at_this_point_this_is_bogus()
            } catch (exception: IOException) {
                exception.printStackTrace()
            }
        } else {
            val dir = File(this.context?.filesDir.toString() + "/tracks")
            showFiles(dir.listFiles())
        }

        mtag = tag
        this.scale = requireContext().resources.displayMetrics.density
        this.pixels = (100 * this.scale + 0.5f).toInt()
        parentLayout = ConstraintLayout(inflater.context);
        mMapView = MapView(inflater.context);
        mMapView.tag = "Map_View";
        mMapView.id = R.id.Map_View;
        //mMapView.addView(parentLayout);

        startButton = Button(parentLayout.context);
        endButton = Button(parentLayout.context);
        pauseButton = Button(parentLayout.context);
        unpauseButton = Button(parentLayout.context);
        addWaypointButton = Button(parentLayout.context);
        //startButton.
        parentLayout.addView(startButton);
        parentLayout.addView(addWaypointButton);
        parentLayout.addView(endButton);
        parentLayout.addView(pauseButton);
        parentLayout.addView(unpauseButton);

        //do some voodoo to make button appear on top of map
        this.activity?.addContentView(parentLayout, ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT,
        ));
        //Log.d("nav",mMapView.parent.toString())
        return mMapView;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState);

        mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mMapView)
        mLocationOverlay.enableMyLocation();
        mMapView.overlays.add(this.mLocationOverlay);

        bogusCopyrightOverlay = CopyrightOverlay(context);
        mMapView.overlays.add(this.bogusCopyrightOverlay);

        mRotationGestureOverlay = RotationGestureOverlay(mMapView)
        mRotationGestureOverlay.isEnabled = true;
        mMapView.overlays.add(mRotationGestureOverlay);

        mMapView.setMultiTouchControls(true);
        mMapView.isTilesScaledToDpi = true;

        mMapView.setTileSource(TileSourceFactory.USGS_TOPO)

        val mapController = mMapView.controller;
        mLocationOverlay.runOnFirstFix {
            this.activity?.runOnUiThread {
                mapController.setZoom(15.0);
                mapController.setCenter(mLocationOverlay.myLocation);
                mapController.animateTo(mLocationOverlay.myLocation);
            }
        }
        Log.d("nav",mMapView.parent.toString())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart(){
        super.onStart();

        Log.d("fragment","started");
        if (isService(TrackerService::class.java)) {
            startButton.visibility = GONE
            endButton.visibility = View.VISIBLE
        } else {
            endButton.visibility = GONE
            startButton.visibility = View.VISIBLE
        }

        startButton.text = "Start";
        endButton.text = "End";
        pauseButton.text = "Pause";
        unpauseButton.text = "Unpause";
        addWaypointButton.text = "Add Waypoint";
        pauseButton.visibility = GONE;
        unpauseButton.visibility = GONE;
        addWaypointButton.visibility = GONE;

        val filter = IntentFilter();
        filter.addAction(BROADCAST_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        requireActivity().registerReceiver(TrackReceiver, filter);

        startButton.setOnClickListener{
            myTrack = Polyline(mMapView);
            mMapView.overlays.add(myTrack);
            requireActivity().startForegroundService(Intent(activity, TrackerService::class.java));
            //requireActivity().bindService(Intent(activity, TrackerService::class.java), connection, Context.BIND_AUTO_CREATE);
            //val msg = Message.obtain(null, MessageValues.MSG_REGISTER_CLIENT.value);
           // mMessenger.send(msg);
            isBound = true;
            tracking = true;
            startButton.visibility = GONE
            endButton.visibility = View.VISIBLE
            pauseButton.visibility = View.VISIBLE;
            addWaypointButton.visibility = View.VISIBLE;
            if(!gpxFile.exists()){
                gpx = GPX("run_track", "1.1");
            }else{
                val bogusGPXParser = SimpleGPXParser(gpxFile.fileName)
                gpx = bogusGPXParser.parseGPX()
            }
            val startlocation = GPXParserLocation(
                    mLocationOverlay.myLocation.latitude,
                    mLocationOverlay.myLocation.longitude,
                    mLocationOverlay.myLocation.altitude,
                    LocalDateTime.now())
            val startpoint = GPXWaypoint(startlocation)
            startpoint.name = "Start"
            gpx.addWaypoint(startpoint)
            Log.d("track:","starte");
            //val handler = Handler():
            
        }

        pauseButton.setOnClickListener{
            tracking = false;
            pauseButton.visibility = GONE;
            unpauseButton.visibility = View.VISIBLE;
            addWaypointButton.visibility = GONE;
            gpx.addWaypoint(GPXWaypoint(
                GPXParserLocation(mLocationOverlay.myLocation.latitude,
                    mLocationOverlay.myLocation.longitude,
                    mLocationOverlay.myLocation.altitude,
                    LocalDateTime.now())
            ));
        }

        unpauseButton.setOnClickListener{
            unpauseButton.visibility = GONE;
            pauseButton.visibility = View.VISIBLE;
            addWaypointButton.visibility = VISIBLE;
            tracking = true;
        }

        addWaypointButton.setOnClickListener{
            gpx.addWaypoint(GPXWaypoint(
                GPXParserLocation(mLocationOverlay.myLocation.latitude,
                    mLocationOverlay.myLocation.longitude,
                    mLocationOverlay.myLocation.altitude,
                    LocalDateTime.now())
            ));
        }

        endButton.setOnClickListener{
            tracking = false;
            val endlocation = GPXParserLocation(
                mLocationOverlay.myLocation.latitude,
                mLocationOverlay.myLocation.longitude,
                mLocationOverlay.myLocation.altitude,
                LocalDateTime.now())
            val endpoint = GPXWaypoint(endlocation)
            endpoint.name = "End"
            if(gpxFile.exists()){
                //gpx = GPX("run_track", "1.1");
                println("exosts")
                val bogusGPXParser = SimpleGPXParser(gpxFile.fileName)
                gpx = bogusGPXParser.parseGPX()
                gpxFile.deleteGPXFile()
            }
            gpx.addWaypoint(endpoint)
            //mMessenger.send(Message.obtain(null, MessageValues.MSG_UNREGISTER_CLIENT.value));
            //if (isBound!!){
            //requireActivity().unbindService(connection);
            Log.d("servicer", "stopped")
            requireActivity().stopService(Intent(activity,TrackerService::class.java));
            mMapView.overlays.remove(myTrack);
            startButton.visibility = View.VISIBLE;
            endButton.visibility = GONE;
            pauseButton.visibility = GONE;
            addWaypointButton.visibility = GONE;
            val nameTrackDialogue = NameTrackDialogue()
            nameTrackDialogue.show(childFragmentManager, nameTrackDialogue.tag)
        }
    }

    override fun onPause(){
        super.onPause();
        mMapView.onPause();
        mLocationOverlay.disableMyLocation();
        /*if (isService(TrackerService::class.java)){
            val msg = Message.obtain(null, MessageValues.MSG_UNREGISTER_CLIENT.value);
            mService!!.send(msg);
        }*/
        Log.d("fragment","pause");
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume();
        mLocationOverlay.enableMyLocation();
        startButton.setOnClickListener{
            requireActivity().startForegroundService(Intent(activity, TrackerService::class.java));
            //requireActivity().bindService(Intent(activity, TrackerService::class.java),connection, Context.BIND_AUTO_CREATE);
            startButton.visibility = GONE
            endButton.visibility = View.VISIBLE
            /*if(gpxFile.createNewFile()){

            }else{
                val bogusGPXParser = SimpleGPXParser(gpxFile.fileName)
                gpx = bogusGPXParser.parseGPX()
            }*/gpx = GPX("run_track", "1.1");
            val startlocation = GPXParserLocation(
                mLocationOverlay.myLocation.latitude,
                mLocationOverlay.myLocation.longitude,
                mLocationOverlay.myLocation.altitude,
                LocalDateTime.now())
            val startpoint = GPXWaypoint(startlocation)
            startpoint.name = "Start";
            gpx.addWaypoint(startpoint)
            Log.d("track:","starte");
        }
        Log.d("fragment","resume");
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMapView.onDetach()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("fragment","saving state")
        if (this::gpx.isInitialized){
            val saveGPX = createParcelableGPX(gpx)
            outState.putParcelable("Saved_GPX", saveGPX)
        }
        if (isService(TrackerService::class.java)){
            outState.putParcelable("Service", mService);
        }
    }

    override fun onStop() {
        super.onStop();
        Log.d("fragment","stopped")
       /* if (isService(TrackerService::class.java)) {
            println("tracking")
            writeToGPX("temp1");

        }*/
    }

    override fun onDestroy() {
        super.onDestroy();
        Log.d("fragment","destroye")
    }

    @SuppressWarnings("Deprecated")
    private fun isService(serviceClass: Class<*>): Boolean {
        val manager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    @SuppressLint("SdCardPath")
    @Throws(IOException::class)
    private fun writeToGPX(trackName: String) {
        val fileName = this.context?.filesDir.toString() + "/tracks/" + trackName + ".gpx"
        Log.d("filefolder", this.context.toString())
        Log.d("filefolder", fileName)
        var bogusGPXParser = SimpleGPXParser(fileName)
        if(bogusGPXParser.createNewFile()){
            Log.d("filefolder","found")
        }else{
            bogusGPXParser = SimpleGPXParser("/data/user/0/com.lazarus.run_track1/files/tracks/" + trackName + ".gpx")
        }
        bogusGPXParser.connectGPX(gpx)
        bogusGPXParser.writeGPX()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Throws(IOException::class)
    private fun i_really_just_do_not_know_how_to_name_this_function_and_i_am_just_too_tired_to_care_at_this_point_this_is_bogus() {
        fileNames = ArrayList()
        val dir = Paths.get(this.requireContext().filesDir.toString() + "/tracks")
        Files.walk(dir).forEach { path: Path ->
            fileNames.add(
                showFile(path.toFile())
            )
        }
    }

    private fun showFiles(files: Array<File>) {
        fileNames = ArrayList()
        for (i in files.indices) {
            fileNames.add(files[0].name)
        }
    }

    private fun showFile(file: File): String {
        return file.name
    }

    /*Name Track Dialogue*/
    override fun onDialogPositiveClick(dialog: DialogFragment, trackName: String?) {
        Log.d("say", trackName!!)
        requireActivity().stopService(Intent(activity, NameTrackDialogueService::class.java))
        try {
            writeToGPX(trackName!!)
        } catch (exception: IOException) {
            exception.printStackTrace()
        }
    }

    override fun onDestroy(localDateTime: LocalDateTime?) {
        requireActivity().stopService(Intent(activity, NameTrackDialogueService::class.java))
        val pattern = "yyyy-MM-dd_HH:mm:ss.SSS"
        val trackName: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localDateTime!!.format(DateTimeFormatter.ofPattern(pattern))
        } else {
            val format: DateFormat = SimpleDateFormat(pattern)
            format.format(Calendar.getInstance().time)
        }
        Log.d("sayy", trackName)
        try {
            writeToGPX(trackName)
        } catch (exception: IOException) {
            exception.printStackTrace()
        }
    }
}