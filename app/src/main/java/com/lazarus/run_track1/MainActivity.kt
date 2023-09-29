package com.lazarus.run_track1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationBarView
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.lazarus.run_track1.HActivitiesFragment.HActivityFragment
import com.lazarus.run_track1.MapsFragment.MapFragment
import com.lazarus.run_track1.NativeJNI.MonJNI
import com.lazarus.run_track1.SettingsFragment.SettingsFragment
import com.lazarus.run_track1.databinding.ActivityMainBinding
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import java.io.File


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
        private set
    private var mMapView:MapView? = null;
    private var mScrollTest:ConstraintLayout? = null;

    fun get_map_view():MapView{
        if (mMapView == null){
            mMapView = MapView(this.applicationContext);
        }
        return MapView(this.applicationContext);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val jni = MonJNI().helloWorld();
        println(jni);

        binding = ActivityMainBinding.inflate(layoutInflater)

        if (!werePermissionsGranted()!!) {
            Log.d("Permissions:", "Requesting");
            requestPermissions()
        }
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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

        val bottomNavigationView = binding.bottomNavigation;
        bottomNavigationView.setOnItemSelectedListener(navListener);

        val test: MapFragment? =
            supportFragmentManager.findFragmentByTag("MAP_FRAGMENT") as MapFragment?

        //load MapFragment. No need for XML
        if(savedInstanceState == null || savedInstanceState.get("Fragment Loaded") != true){
            Log.d("fragment","duplicating")
            val manager: FragmentManager = supportFragmentManager;
            val transaction: FragmentTransaction = manager.beginTransaction();
            transaction.add(R.id.fragment_container, MapFragment(), "MAP_FRAGMENT");
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
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            var selectedFragment: Fragment? = null

            Log.d("nav","selected");

            when (item.itemId) {
                R.id.nav_home -> {
                    findViewById<Button>(R.id.start).visibility = VISIBLE;
                    selectedFragment = MapFragment()
                    changeFragment(selectedFragment, "Fragment")
                }
                R.id.nav_activities -> {
                    findViewById<Button>(R.id.start).visibility = GONE;
                    selectedFragment = HActivityFragment()
                    changeFragment(selectedFragment, "Activities")
                }
                R.id.nav_settings -> {
                    findViewById<Button>(R.id.start).visibility = GONE;
                    selectedFragment = SettingsFragment()
                    changeFragment(selectedFragment, "Settings")
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

    private fun requestPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.FOREGROUND_SERVICE
            ),
            69420
        );
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
        return GPSPermissionGranted && ReadWriteFiles
    }
}

fun usineLiaisonActivité(activité:FragmentActivity?):ActivityMainBinding{
    val activitéPrincipale = activité as MainActivity
    return activitéPrincipale.binding
}

fun loadLibrary(){
    System.loadLibrary("native");
}