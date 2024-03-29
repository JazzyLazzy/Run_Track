package com.lazarus.run_track1

import SimpleGPX.TrackPoint
import android.Manifest
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import com.google.firebase.initialize
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storage
import com.lazarus.run_track1.HActivitiesFragment.HActivityFragment
import com.lazarus.run_track1.MapsFragment.MapFragment
import com.lazarus.run_track1.MapsFragment.roundTo3DecimalPlaces
import com.lazarus.run_track1.SettingsFragment.SettingsFragment
import com.lazarus.run_track1.databinding.ActivityMainBinding
import com.lazarus.simplecpxwrapper.NativeLib
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.time.Instant
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
        private set
    private var mMapView:MapView? = null;
    private var mScrollTest:ConstraintLayout? = null;
    private lateinit var mMapFragment:MapFragment;

    val TrackReceiver: BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            Log.d("intentinca", action ?: "no Action")
            mMapFragment.handleBroadcastRecieved(context, intent);
        }
    }

    fun get_map_view():MapView{
        if (mMapView == null){
            mMapView = MapView(this.applicationContext);
        }
        return MapView(this.applicationContext);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val native = NativeLib();
        //native.wtb(applicationContext.filesDir.toString() + "/tracks/waypoints.gpx")

        binding = ActivityMainBinding.inflate(layoutInflater)

        if (!werePermissionsGranted()!!) {
            Log.d("Permissions:", "Requesting");
            requestPermissions()
        }
        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork;

        Log.d("fragment","activity created")
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(binding.root);

        val file = File(this.filesDir.toString() + "/tracks")
        if (!file.exists()) {
            // create the folder
            val result = file.mkdir()
            if (result) {
                println("Successfully created " + file.absolutePath)
            } else {
                println("Failed creating " + file.absolutePath)
            }
        } else {
            println("Pathname already exists")
        }

        //Initialise Bouncy Castle
        initialiseProvider();
        //Generate Keys
        initialiseEncryption(this.applicationContext);

        val iFitler = IntentFilter();
        iFitler.addAction("LOCATION_UPDATE")
        iFitler.addAction("SAVE_GPX")
        iFitler.addAction("action")
        this.registerReceiver(TrackReceiver, iFitler)

        sendBroadcast(Intent("action"))

        val bottomNavigationView = binding.bottomNavigation;
        bottomNavigationView.setOnItemSelectedListener(navListener);

        //load MapFragment. No need for XML
        if(savedInstanceState == null || savedInstanceState.get("Fragment Loaded") != true){
            Log.d("fragment","duplicating")
            val manager: FragmentManager = supportFragmentManager;
            val transaction: FragmentTransaction = manager.beginTransaction();
            mMapFragment = MapFragment()
            transaction.add(R.id.fragment_container, mMapFragment, "MAP_FRAGMENT");
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("Fragment Loaded", true)
    }

    //Bottom Navigation View handler/listener/dooer thingamabogus
    private val navListener:NavigationBarView.OnItemSelectedListener = object : NavigationBarView.OnItemSelectedListener {

        private var flips = -1;

        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            var selectedFragment: Fragment? = null

            Log.d("nav","selected");

            when (item.itemId) {
                R.id.nav_home -> {
                    findViewById<Button>(R.id.start).visibility = VISIBLE;
                    mMapFragment = MapFragment()
                    flips += 1
                    Log.d("flps", flips.toString())
                    changeFragment(mMapFragment, "Fragment$flips")
                }
                R.id.nav_activities -> {
                    findViewById<Button>(R.id.start).visibility = GONE;
                    selectedFragment = HActivityFragment()
                    flips += 1
                    changeFragment(selectedFragment, "Activities$flips")
                }
                R.id.nav_settings -> {
                    findViewById<Button>(R.id.start).visibility = GONE;
                    selectedFragment = SettingsFragment()
                    flips += 1
                    changeFragment(selectedFragment, "Settings$flips")
                }
            }
            return true
        }

        private fun changeFragment(selectedFragment: Fragment, tag: String) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, selectedFragment, tag)
                .commit()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            69420 -> {
                if (!(grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d("Permission","Not Granted");
                }
                return;
            }
        }
    }

    override fun onPause(){
        super.onPause();
        Log.d("activityy", "paused");
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(TrackReceiver)
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.FOREGROUND_SERVICE)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            permissions.plus(Manifest.permission.POST_NOTIFICATIONS);
        }
        requestPermissions(permissions, 69420);
    }

    private fun werePermissionsGranted(): Boolean? {
        Log.v("boguss", "checkinggps")
        val GPSPermissionGranted = (ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED)
        val ReadWriteFiles = (ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) ==
                PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) ==
                PackageManager.PERMISSION_GRANTED)
        val Notifications:Boolean = if (android.os.Build.VERSION.SDK_INT >= 33) (ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        )) ==   PackageManager.PERMISSION_GRANTED else true
        return GPSPermissionGranted && ReadWriteFiles && Notifications
    }
}

fun usineLiaisonActivité(activité:FragmentActivity?):ActivityMainBinding{
    val activitéPrincipale = activité as MainActivity
    return activitéPrincipale.binding
}

fun loadLibrary(){
    System.loadLibrary("native");
}