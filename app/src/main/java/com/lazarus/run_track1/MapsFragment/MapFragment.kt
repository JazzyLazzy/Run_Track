package com.lazarus.run_track1.MapsFragment

//import kotlinx.android.synthetic.main.activity_main.*
//import kotlinx.android.synthetic.main.activity_main.view.*

import SimpleGPX.*
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
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
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lazarus.run_track1.*
import com.lazarus.run_track1.HActivitiesFragment.loadTrack
import com.lazarus.run_track1.Service.BoundTrackerService
import com.lazarus.run_track1.Service.NameTrackDialogue
import com.lazarus.run_track1.Service.NameTrackDialogueService
import com.lazarus.run_track1.Service.TrackerService
import com.lazarus.run_track1.databinding.MapFragmentBinding
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.w3c.dom.Text
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.*
import kotlin.time.measureTime


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
    private lateinit var statsLayout: StatsLayout;
    private lateinit var bLl:LinearLayout;
    private lateinit var parentLayout: ConstraintLayout;
    private lateinit var fileNames:ArrayList<String>;
    private lateinit var binding:MapFragmentBinding;
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
    private lateinit var foregroundService: TrackerService;
    private var isServiceBound = false
    private var distanceTraveled = 0.0;
    private var wdistanceTraveled = 0.0;
    private var timeElapsed = 0;
    private var elevationGained = 0.0;
    private var welevationGained = 0.0;
    private var previousElevation:Double? = null;
    private var previousLocation:GeoPoint? = null;
    private var originalTime:Long? = null;
    private var tService: TrackerService? = null;
    private var waypointTime:Long? = null;

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TrackerService.TrackerBinder;
            tService = binder.getService()
            gpx = loadGPXFromService();
            myTrack = Polyline(mMapView);
            loadTrack(gpx, myTrack!!);
            mMapView.overlays.add(myTrack);
            for (i in gpx.tracks){
                for (j in i.trksegs){
                    Log.d("pt1", j.trkpts.first().elevation.toString());
                    Log.d("pt2", j.trkpts.last().elevation.toString());
                    val statStruct = calculateStats(j);
                    Log.d("distance_tot", statStruct.distance.toString());
                    Log.d("ele diff ", statStruct.ElevationStruct().totalElevation.toString());
                    distanceTraveled += statStruct.distance;
                    elevationGained += statStruct.ElevationStruct().totalElevation;
                }
            }
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }
    }

    private fun loadGPXFromService():GPX{
        return tService!!.sendLocationData();
    }

    private val TrackReceiver:BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val trkpts = ArrayList<TrackPoint>()
            if (intent!!.action == "LOCATION_UPDATE"){
                val bundle = intent.getBundleExtra("data point");
                val trkPoint = bundle!!.getParcelable<ParcelableTrackPoint>("track_point")
                if (trkPoint != null) {
                    val geoPoint =
                        GeoPoint(trkPoint.latitude, trkPoint.longitude, trkPoint.elevation);
                    if (myTrack != null) {
                       // Log.d("debug", myTrack.toString());
                        myTrack?.addPoint(geoPoint);
                    } else {
                        myTrack = Polyline(mMapView);
                        mMapView.overlays.add(myTrack);
                        myTrack?.addPoint(geoPoint);
                    }
                    if (previousElevation == null) {
                        previousElevation = trkPoint.elevation;
                    } else {
                        elevationGained += trkPoint.elevation - previousElevation!!;
                        welevationGained += trkPoint.elevation - previousElevation!!;
                        previousElevation = trkPoint.elevation;
                    }
                    if (previousLocation == null) {
                        previousLocation = geoPoint;
                    } else {
                        distanceTraveled += haversineDistance(previousLocation!!, geoPoint);
                        wdistanceTraveled += haversineDistance(previousLocation!!, geoPoint);
                        previousLocation = geoPoint;
                    }
                    requireActivity().findViewById<TextView>(R.id.avancement_au_hauteur)?.text =
                        roundTo3DecimalPlaces(elevationGained) + "\n" + roundTo3DecimalPlaces(welevationGained);
                    requireActivity().findViewById<TextView>(R.id.avancement_à_distance)?.text =
                        roundTo3DecimalPlaces(distanceTraveled / 1000) + "\n" + roundTo3DecimalPlaces(wdistanceTraveled/1000);
                    if (android.os.Build.VERSION.SDK_INT >= 26) {
                        /*if (originalTime == null){
                            originalTime = GPXParserLocation.time_to_long(gpx.waypoints[0].time)
                            waypointTime = GPXParserLocation.time_to_long(gpx.waypoints[gpx.waypoints.size - 1].time)
                        }*/
                        val currentPace = roundTo3DecimalPlaces((((Instant.now().epochSecond - originalTime!!)) / 60) / ((distanceTraveled + 0.001) / 1000))
                        val waypointPace = roundTo3DecimalPlaces((((Instant.now().epochSecond - waypointTime!!)) / 60) / ((wdistanceTraveled + 0.001) / 1000))

                        requireActivity().findViewById<TextView>(R.id.avancement_aux_temps)?.text = currentPace
                        requireActivity().findViewById<TextView>(R.id.wavancement_aux_temps)?.text = waypointPace;

                    }
                }
            }else{
                val fileName = intent.getStringExtra("trackpoints")
                Log.d("bundle",fileName!!)
                val readGPX = SimpleGPXParser("$fileName")
                //Log.d("bundle", Stringify(fileName))
                val tracks = readGPX.parseGPX().tracks
                readGPX.deleteGPXFile();
                Log.d("bundle", tracks[0].name!!)
                gpx.addTracks(tracks)
            }

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

    //@SuppressLint("HandlerLeak")
    private val gestionnaireEntrant = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MessageValues.MSG_REÇEVOIR_TRKPT.value -> {
                    val trkPoint =
                        msg.data.getParcelable<ParcelableTrackPoint>("track_point") as TrackPoint;
                    //gpx.addTrackPointToEnd(trkPoint);
                    val geoPoint =
                        GeoPoint(trkPoint.latitude, trkPoint.longitude, trkPoint.elevation);
                    if (myTrack != null) {
                        myTrack!!.addPoint(geoPoint);
                    } else {
                        myTrack = Polyline(mMapView);
                        mMapView.overlays.add(myTrack);
                        myTrack!!.addPoint(geoPoint);
                    }
                }
            }
        }
    }

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
            elevationGained = savedInstanceState.getDouble("Dénivelé positif");
            distanceTraveled = savedInstanceState.getDouble("Distance voyagée");
            /*requireActivity().bindService(Intent(activity, TrackerService::class.java), connection, Context.BIND_AUTO_CREATE);
            val saveService = savedInstanceState.getParcelable<Messenger>("Service");
            if (saveService != null){
                mService = saveService;
                if (mService != null) {
                    mService!!.send(Message.obtain(null, MessageValues.MSG_REGISTER_CLIENT.value));
                }
            }*/
        }

        if (isService(TrackerService::class.java)){

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MapFragmentBinding.inflate(inflater);
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
        //mMapView = binding.mainMapView;
        //mMapView.tag = "Map_View";
       // mMapView.id = R.id.Map_View;
        //parentLayout = binding.parentLayout;
       // mMapView.addView(parentLayout);
        parentLayout = binding.root;

        statsLayout = StatsLayout(this.requireContext());
        //startButton.
        parentLayout.addView(statsLayout);
        //set_stats_layout_constraints(statsLayout);

        //do some voodoo to make button appear on top of map
       /*this.activity?.addContentView(parentLayout, ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT,
        ));
        //Log.d("nav",mMapView.parent.toString())*/
        return parentLayout;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState);

        mMapView = (this.activity as MainActivity).get_map_view();
        parentLayout.addView(mMapView);
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

        if (myTrack != null){
            mMapView.overlays.add(myTrack);
        }
        Log.d("nav",mMapView.parent.toString())
    }

    private fun loadPreviousTrack(){
        startButton.visibility = GONE
        endButton.visibility = View.VISIBLE;
        addWaypointButton.visibility = VISIBLE;
        accèsVuesActivité(activity, GONE);
        val intent = Intent(context, TrackerService::class.java)
        requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart(){
        super.onStart();

        startButton = this.requireActivity().findViewById(R.id.start);
        addWaypointButton = this.requireActivity().findViewById(R.id.waypoint);
        endButton = this.requireActivity().findViewById(R.id.stop);


        Log.d("fragment","started");
        if (isService(TrackerService::class.java)) {
            loadPreviousTrack()
        } else {
            endButton.visibility = GONE
            startButton.visibility = View.VISIBLE
            addWaypointButton.visibility = GONE;
            accèsVuesActivité(activity, VISIBLE);
            //l_avancement.visibility = GONE;
        }


        val filter = IntentFilter();
        filter.addAction(BROADCAST_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        requireActivity().registerReceiver(TrackReceiver, filter);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(TrackReceiver, IntentFilter("LOCATION_UPDATE"))

        startButton.setOnClickListener{
            startButtonClicked();
        }

        /*pauseButton.setOnClickListener{
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
        }*/

        addWaypointButton.setOnClickListener{
            gpx.addWaypoint(GPXWaypoint(
                GPXParserLocation(mLocationOverlay.myLocation.latitude,
                    mLocationOverlay.myLocation.longitude,
                    mLocationOverlay.myLocation.altitude,
                    LocalDateTime.now())
            ));
            val newWaypoint = Marker(mMapView);
            newWaypoint.position = mLocationOverlay.myLocation;
            newWaypoint.setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_CENTER);
            mMapView.overlays.add(newWaypoint);
            wdistanceTraveled = 0.0;
            welevationGained = 0.0;
            waypointTime = Instant.now().epochSecond;
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
            for (i in mMapView.overlays){
                if (i is Marker){
                    mMapView.overlays.remove(i);
                }
            }
            Log.d("servicer", "stopped")
            requireActivity().stopService(Intent(activity, TrackerService::class.java));
            mMapView.overlays.remove(myTrack);
            startButton.visibility = View.VISIBLE;
            endButton.visibility = GONE;
            addWaypointButton.visibility = GONE;
            statsLayout.visibility = GONE;
            accèsVuesActivité(activity, VISIBLE);
            val nameTrackDialogue = NameTrackDialogue()
            nameTrackDialogue.show(childFragmentManager, nameTrackDialogue.tag)
            elevationGained = 0.0;
            previousElevation = null;
            previousLocation = null;
            distanceTraveled = 0.0;
        }
    }

    override fun onPause(){
        super.onPause();
        mMapView.onPause();
        //mLocationOverlay.disableMyLocation();
        /*if (isService(TrackerService::class.java)){
            val msg = Message.obtain(null, MessageValues.MSG_UNREGISTER_CLIENT.value);
            mService!!.send(msg);
        }*/
        Log.d("fragment","pause");
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume();
        startButton.setOnClickListener{
            startButtonClicked()
        }
        Log.d("fragment","resume");
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMapView.onDetach()
    }

    private fun startButtonClicked(){
        ContextCompat.startForegroundService(this.requireContext(), Intent(activity, TrackerService::class.java));
        startButton.visibility = GONE
        endButton.visibility = View.VISIBLE
        addWaypointButton.visibility = View.VISIBLE;
        Log.d("view", binding.root.visibility.toString());
        accèsVuesActivité(activity, GONE);
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
        myTrack = Polyline(mMapView);
        mMapView.overlays.add(myTrack);
        originalTime = Instant.now().epochSecond;
        waypointTime = Instant.now().epochSecond;
        Log.d("track:","starte");
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
        outState.putDouble("Dénivelé positif", elevationGained);
        outState.putDouble("Distance voyagée", distanceTraveled);
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
        requireActivity().unregisterReceiver(TrackReceiver);
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(TrackReceiver)
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

    // Vraiment, même en regardant en arrière sur cela, je comprends ce que cette fonction fait-elle,
    // mais je ne sais pas comment s'appeler. Si vous ne savez pas ce qu'elle fait, elle trouve tous les noms des fichiers
    // dans "/tracks" et les ajoute à fileNames, qui est un Tableau (ArrayList).
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

    private fun haversineDistance(coord1: GeoPoint, coord2: GeoPoint): Double {
        val R = 6371e3 // Earth's radius in meters
        val lat1Rad = Math.toRadians(coord1.latitude)
        val lat2Rad = Math.toRadians(coord2.latitude)
        val latDiff = Math.toRadians(coord2.latitude - coord1.latitude)
        val lonDiff = Math.toRadians(coord2.longitude - coord1.longitude)

        val a = sin(latDiff / 2) * sin(latDiff / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(lonDiff / 2) * sin(lonDiff / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        val altitudeDiff = coord2.altitude - coord1.altitude

        return sqrt((R * R * c * c) + (altitudeDiff * altitudeDiff))
    }
}

fun accèsVuesActivité(activity: FragmentActivity?, bottomNavigationVisibility:Int) {
    val activityBinding = usineLiaisonActivité(activity)

    // Access the activity's views as needed
    activityBinding.bottomNavigation.visibility = bottomNavigationVisibility
    activityBinding.lAvancement.visibility = -1 * (bottomNavigationVisibility - 8);
}

fun roundTo3DecimalPlaces(value: Double): String {
    return String.format("%.3f", value)
}