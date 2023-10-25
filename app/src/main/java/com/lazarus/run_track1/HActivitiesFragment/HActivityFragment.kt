package com.lazarus.run_track1.HActivitiesFragment

import SimpleGPX.SimpleGPXParser
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.lazarus.run_track1.R
import com.lazarus.run_track1.databinding.HActivityFragmentBinding
import com.lazarus.run_track1.HActivitiesFragment.AdaptateurListeActivités
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.math.absoluteValue


class HActivityFragment : Fragment(), AdaptateurListeActivités.EnInfoActivitéClicquéÉcouteur {

    //private var binding: ActivitiesBinding? = null
    private val activities = ArrayList<OpenAppCompatButton>();
    private val containers = ArrayList<ConstraintLayout>();
    private var gpx: SimpleGPX.GPX? = null
    private var viewActivityFragment: ActivityMapFragment? = null
    //private var passGPX: PassGPX? = null
    private var mtag: String? = null
    private var scale = 0f
    private var pixels = 0
    private var fileNames: ArrayList<String>? = null
    private lateinit var parentLayout:LinearLayout;
    private lateinit var binding:HActivityFragmentBinding;
    private lateinit var vueRecyclage: RecyclerView;
    private lateinit var constraintSet: ConstraintSet;
    private val storage = Firebase.storage;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mtag = tag
        scale = requireContext().resources!!.displayMetrics.density
        pixels = (100 * scale + 0.5f).toInt();
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HActivityFragmentBinding.inflate(inflater);
        vueRecyclage = binding.hActivityScroll;
        Log.d("nav","viewcreatedh")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                findFiles()
            } catch (exception: IOException) {
                exception.printStackTrace()
            }
        } else {
            val dir = File(this.context?.filesDir.toString() + "/tracks")
            showFiles(dir.listFiles())
        }
        constraintSet = ConstraintSet();
        for (i in fileNames!!.indices) {
            //constraintSet = createButton(i, constraintSet);
        }

        //this.activity?.setContentView(binding.root);
        val adaptateurActivités = AdaptateurListeActivités(this.requireContext(), fileNames!!, ::enCliquéAdaptateur, ::mettreAuCloud);
        vueRecyclage.layoutManager = LinearLayoutManager(this.activity);
        vueRecyclage.adapter = adaptateurActivités;
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        println("started h view");
        /*for (i in activities.indices) {
            activities[i].setOnClickListener { view: View? ->
                val summary = LinearLayoutCompat(this.requireContext());
                summary.setBackgroundColor(Color.rgb(0,0,125));
                summary.layoutParams =
                    LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
                summary.orientation = LinearLayoutCompat.VERTICAL;
                val speed_txt = TextView(this.requireContext());
                speed_txt.setText("Speed");
                summary.id = View.generateViewId();
                summary.addView(speed_txt);
                summary.setBackgroundColor(Color.rgb(245, 200, 48))
                containers[i].addView(summary);
                val containerSet = ConstraintSet();
                containerSet.clone(containers[i]);
                Log.d("constraint_layout_click", "before connect");
                containerSet.connect(summary.id, ConstraintSet.TOP, containers[i].id, ConstraintSet.BOTTOM);
                Log.d("constraint_layout_click", "before connect 1");
                containerSet.connect(summary.id, ConstraintSet.BOTTOM, containers[i + 1].id, ConstraintSet.TOP);
                containerSet.applyTo(constraintLayout);
                //constraintSet.clone(constraintLayout);
                //constraintSet.applyTo(constraintLayout);
                //Log.d("time_click", LocalDateTime.now().toString())
                Log.d("constraint_layout_click", summary.id.toString());
                Log.d("constraint_layout_click", activities[i].id.toString());
                Log.d("constraint_layout_click", activities[i+1].id.toString());
                /*activityMapFragment = ActivityMapFragment();
                val trackName = activities[i].text;
                val fileName =
                    this.context?.filesDir.toString() + "/tracks/$trackName";
                val fileNameBundle = Bundle();
                fileNameBundle.putString("gpx_file",fileName);
                Log.d("time_putstring" , LocalDateTime.now().toString())
                val bogusGPXParser = SimpleGPXParser("file://$fileName")
                val newTag = "track"
                viewActivityFragment!!.arguments = fileNameBundle;
                //if (gpx != null) {
                    this.requireActivity().supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, activityMapFragment!!, newTag)
                        .commit()
                //}*/
                //test_RemoveButton(activities[i])
            }
        }*/
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
    private fun findFiles() {
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

    /*private fun createButton(i:Int, constraintSet: ConstraintSet):ConstraintSet{
        val container = ConstraintLayout(this.requireContext());
        container.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.id = View.generateViewId();
        val activity = OpenAppCompatButton(this.requireContext());
        activity.setBackgroundColor(Color.rgb(24, 200, 48))
        activity.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        activity.height = pixels;
        activity.alpha = 1.0f;
        activity.text = fileNames!![i];
        activity.setTextColor(Color.rgb(255, 0, 54));
        activity.gravity = Gravity.CENTER_VERTICAL;
        activity.id = View.generateViewId();
        //activity.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        val settings = AppCompatButton(this.requireContext());
        val svg = resources.getDrawable(R.drawable.ic_drawing, null);
        settings.background = svg;
        container.addView(activity);

        //container.addView(settings);
        //container.minWidth = ViewGroup.LayoutParams.MATCH_PARENT;
        binding.hActivityScroll.addView(container);
        //binding.hActivityScroll.addView(activity);
        activities.add(activity);
        containers.add(container);
        if (i > 0){
            constraintSet.clone(constraintLayout)
            constraintSet.connect(container.id, ConstraintSet.TOP, containers[i - 1].id, ConstraintSet.BOTTOM, 0)
            constraintSet.applyTo(constraintLayout);
        }
        return constraintSet;
    }*/

    /*private fun createSettingsButton(){
        val settingsButton = AppCompatButton(this.requireContext());
        settings
    }*/

    // Test code
    private fun test_RemoveButton(appCompatButton: AppCompatButton){
        appCompatButton.visibility = View.GONE;
    }

    override fun enInfoActivitéClicqué(nomFichier: String) {
        /*val activityMapFragment = ActivityMapFragment();
        val fileName = this.context?.filesDir.toString() + "/tracks/$nomFichier"
        val fileNameBundle = Bundle().apply {
            putString("gpx_file", fileName)
        }

        val bogusGPXParser = SimpleGPXParser("file://$fileName")
        val newTag = "track"

        activityMapFragment.arguments = fileNameBundle

        this.requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, activityMapFragment, newTag)
            .commit()*/
    }

    private fun enCliquéAdaptateur(nomFichier: String){
        val activityMapFragment = ActivityMapFragment();
        val fileName = this.context?.filesDir.toString() + "/tracks/$nomFichier"
        val fileNameBundle = Bundle().apply {
            putString("gpx_file", fileName)
        }

        val bogusGPXParser = SimpleGPXParser("file://$fileName")
        val newTag = "track"

        activityMapFragment.arguments = fileNameBundle

        this.requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, activityMapFragment, newTag)
            .addToBackStack(null)
            .commit()
    }

    private fun mettreAuCloud(nomFichier: String){
        val fileName = this.context?.filesDir.toString() + "/tracks/$nomFichier"
        var file = Uri.fromFile(File(fileName));
        var storageRef = storage.reference;
        val fileRef = storageRef.child("images/${file.lastPathSegment}");
        var uploadTask = fileRef.putFile(file);
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Log.d("upload__", "failed");
        }.addOnSuccessListener { taskSnapshot -> Log.d("upload__", taskSnapshot.metadata.toString())
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
    }

}