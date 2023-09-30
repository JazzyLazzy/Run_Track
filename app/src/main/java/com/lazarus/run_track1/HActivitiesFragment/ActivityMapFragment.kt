package com.lazarus.run_track1.HActivitiesFragment

import SimpleGPX.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.lazarus.run_track1.MapsFragment.accèsVuesActivité
import com.lazarus.run_track1.R
import com.lazarus.run_track1.usineLiaisonActivité
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.*


class ActivityMapFragment : Fragment() {

    private lateinit var mMapView: MapView;
    private lateinit var mRotationGestureOverlay: RotationGestureOverlay;
    private lateinit var bogusCopyrightOverlay: CopyrightOverlay;
    private lateinit var mLocationOverlay: MyLocationNewOverlay;
    private lateinit var bLl: LinearLayout;
    private lateinit var parentLayout: ConstraintLayout;
    private lateinit var fileNames: ArrayList<String>;
    private var mtag: String? = null
    private var scale = 0f
    private var pixels = 0
    private lateinit var myTrack:Polyline;
    private lateinit var activityMapFragment:ActivityMapFragment;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        requireActivity().onBackPressedDispatcher.addCallback {
            parentFragmentManager.popBackStack();
            accèsVuesActivité(activity, View.VISIBLE);
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

        //do some voodoo to make button appear on top of map
        this.activity?.addContentView(parentLayout, ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT,
        ));
        //Log.d("nav",mMapView.parent.toString())
        return mMapView;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("time_viewcreate", LocalDateTime.now().toString())

        val liaisonActivité = usineLiaisonActivité(activity);
        liaisonActivité.bottomNavigation.visibility = GONE;
        liaisonActivité.lAvancement.visibility = VISIBLE;

        val plaqueBas = exempleNouveau();
        plaqueBas.show(parentFragmentManager, plaqueBas.tag)


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

        myTrack = Polyline(mMapView);
        val bundle = arguments;
        val fileName = bundle!!.get("gpx_file");
        //val parcelableGPX = bundle!!.getParcelable<ParcelableGPX>("gpx_track");
        val stringify = Stringify("$fileName")
        Log.d("p-point", stringify);

        mMapView.overlays.add(myTrack);
        val bogusGPXParser = SimpleGPXParser("file://$fileName");
        Log.d("time_parse", LocalDateTime.now().toString())
        val parcelableGPX = bogusGPXParser.parseGPX();
        Log.d("time_beginloop", LocalDateTime.now().toString())
        loadTrack(parcelableGPX, myTrack);
        var distance = 0.0;
        var tE = 0.0;
        var eG = 0.0;
        var eL = 0.0;
        var pace = Pace(0, 0.0);
        val statsStruct = StatStruct();
        Log.d("lengths", parcelableGPX.tracks[0].trksegs[0].trkpts.size.toString());
        for (i in parcelableGPX.tracks){
            for (j in i.trksegs){
                Log.d("distance", distance.toString());
                Log.d("pt1", j.trkpts.first().elevation.toString());
                Log.d("pt2", j.trkpts.last().elevation.toString());
                val statStruct = calculateStats(j);
                Log.d("ele diff ", statStruct.ElevationStruct().totalElevation.toString());
                distance += statStruct.distance;
                pace.minutes += statStruct.pace!!.minutes;
                pace.seconds += statStruct.pace!!.seconds;
                tE += statStruct.ElevationStruct().totalElevation;
                eG += statStruct.ElevationStruct().elevationGain;
                eL += statStruct.ElevationStruct().elevationLoss;
            }
        }
        pace.minutes /= parcelableGPX.tracks[0].trksegs.size;
        pace.seconds /= parcelableGPX.tracks[0].trksegs.size;
        afficherLesStatistiques(distance.toString(), tE.toString(), pace.toString());
        Log.d("time_endloop", LocalDateTime.now().toString());
        Log.d("distance",distance.toString());
        Log.d("elevation",tE.toString())
        //Log.d("distance", distance.toString());
        //myTrack.addPoint(GeoPoint(125.2,-42.43, 8.21))
        //myTrack.addPoint(GeoPoint(15.2,2.43, 188.21))

        for (i in parcelableGPX.waypoints){
            val nwaypoint = Marker(mMapView);
            nwaypoint.position = GeoPoint(i.latitude, i.longitude, i.elevation);
            nwaypoint.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mMapView.overlays.add(nwaypoint);
        }

        val mapController = mMapView.controller;
        mLocationOverlay.runOnFirstFix {
            this.activity?.runOnUiThread {
                mapController.setZoom(15.0);
                val latitude = parcelableGPX.waypoints[0].latitude;
                val longitude = parcelableGPX.waypoints[0].longitude;
                mapController.setCenter(GeoPoint(latitude, longitude));
                mapController.animateTo(GeoPoint(latitude, longitude));
            }
        }
        Log.d("nav",mMapView.parent.toString())
    }

    private fun afficherLesStatistiques(distance:String, élévation:String, temps:String){
        var liaisonActivité = usineLiaisonActivité(activity);
        liaisonActivité.avancementAuHauteur.text = élévation;
        liaisonActivité.avancementDistance.text = distance;
        liaisonActivité.avancementAuxTemps.text = temps;
    }

    // Oh, je l'ai copié de MapFragment.
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Throws(IOException::class)
    private fun i_really_just_do_not_know_how_to_name_this_function_and_i_am_just_too_tired_to_care_at_this_point_this_is_bogus() {
        fileNames = ArrayList()
        val dir = Paths.get(this.context?.filesDir.toString() + "/tracks")
        Files.walk(dir).forEach { path: Path ->
            fileNames!!.add(
                showFile(path.toFile())

            )
            Log.d("fiiiles", path.toFile().toString());
        }
    }

    private fun showFiles(files: Array<File>) {
        fileNames = ArrayList()
        for (i in files.indices) {
            fileNames!!.add(files[0].name)
            Log.d("fiiiles", files[0].name);
        }
    }

    private fun showFile(file: File): String {
        return file.name
    }
}

fun loadTrack(parcelableGPX: GPX, myTrack:Polyline){
    for (i in parcelableGPX.tracks.indices){
        Log.d("p-point","hellow")
        for (j in parcelableGPX.tracks[i].trksegs.indices){
            Log.d("p-point","hellowo")
            for (k in parcelableGPX.tracks[i].trksegs[j].trkpts.indices){
                val latitude = parcelableGPX.tracks[i].trksegs[j].trkpts[k].latitude;
                val longitude = parcelableGPX.tracks[i].trksegs[j].trkpts[k].longitude;
                val altitude = parcelableGPX.tracks[i].trksegs[j].trkpts[k].elevation;
                //Log.d("p-point", latitude.toString());
                val geoPoint = GeoPoint(latitude, longitude, altitude);
                myTrack.addPoint(geoPoint);
            }
        }
    }
}