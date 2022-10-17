package com.lazarus.run_track1.HActivitiesFragment

import SimpleGPX.*
import SimpleGPX.SimpleGPXParser

import ll.SimpleGPX.*;

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.lazarus.run_track1.ParcelableGPX
import com.lazarus.run_track1.R
import com.lazarus.run_track1.databinding.HActivityFragmentBinding
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

class HActivityFragment : Fragment() {

    //private var binding: ActivitiesBinding? = null
    private val activities = ArrayList<Button>()
    private var ll_gpx: ll.SimpleGPX.GPX? = null
    private var gpx: SimpleGPX.GPX? = null
    private var viewActivityFragment: ViewActivityFragment? = null
    //private var passGPX: PassGPX? = null
    private var mtag: String? = null
    private var scale = 0f
    private var pixels = 0
    private var fileNames: ArrayList<String>? = null
    private lateinit var parentLayout:LinearLayout;
    private lateinit var binding:HActivityFragmentBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mtag = tag
        scale = requireContext().resources!!.displayMetrics.density
        pixels = (100 * scale + 0.5f).toInt()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HActivityFragmentBinding.inflate(inflater);
        Log.d("nav","viewcreatedh")
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
        for (i in fileNames!!.indices) {
            val activity = Button(this.context)
            activity.setBackgroundColor(Color.rgb(24, 200, 48))
            activity.height = pixels
            activity.alpha = 0.1f
            activity.text = fileNames!![i]
            activity.setTextColor(Color.rgb(255, 0, 54));
            binding.hActivityScroll.addView(activity);
            activities.add(activity)
        }

        //this.activity?.setContentView(binding.root);

        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        for (i in activities.indices) {
            activities[i].setOnClickListener { view: View? ->
                Log.d("time_click", LocalDateTime.now().toString())
                viewActivityFragment = ViewActivityFragment();
                val trackName = activities[i].text;
                val fileName =
                    this.context?.filesDir.toString() + "/tracks/$trackName";
                val fileNameBundle = Bundle();
                fileNameBundle.putString("gpx_file",fileName);
                Log.d("time_putstring" , LocalDateTime.now().toString())
                val bogusGPXParser = SimpleGPXParser("file://$fileName")
                //Log.d("p-point", fileName);
                //val gpxstring  = ll.SimpleGPX.Stringify("$fileName")
                //val path:Uri = Uri.fromFile("file://$fileName")
                //sendEmail("GPX", path, this.context)
                //val llgpxParser = ll.SimpleGPX.SimpleGPXParser("file://$fileName");
                //Log.d("time_start_array_parse",LocalDateTime.now().toString());
                //gpx = bogusGPXParser.parseGPX();
                //Log.d("time_start_list_parse", LocalDateTime.now().toString());
                //ll_gpx = llgpxParser.parseGPX();
                //Log.d("time_parsegpx", LocalDateTime.now().toString())
                //val wpt = gpx!!.waypoints[0]
                //Log.d("wptact", wpt.latitude.toString())
                val newTag = "track"
                //val gpxstring  = Stringify("$fileName")
                //sendEmail("GPX",gpxstring,this.context);


                //val samplegpx = GPXify("")

                //val stringify = Stringify(gpx as GPX, "/data/user/0/com.lazarus.run_track1/files/tracks/ttrest.gpx")
                //Log.d("p-point", stringify);
                //val parcelableGPX = ParcelableGPX(gpx!!.creator, gpx!!.version, gpx!!.waypoints, gpx!!.tracks);
                //val gpxBundle = Bundle();
                //gpxBundle.putParcelable("gpx_track", parcelableGPX);
                //Log.d("time_putparcel", LocalDateTime.now().toString())
                viewActivityFragment!!.arguments = fileNameBundle;
                //Log.d("time_putstring" , LocalDateTime.now().toString())
                //if (gpx != null) {
                    this.requireActivity().supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, viewActivityFragment!!, newTag)
                        .commit()
                //}
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("history", "closed")
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /*fun onFragmentCreated(tag: String) {
        passGPX.passGPX(gpx, tag)
    }*/

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

    private fun deleteTrack(button: Button){

    }

}