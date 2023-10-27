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
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.lazarus.run_track1.R
import com.lazarus.run_track1.databinding.HActivityFragmentBinding
import com.lazarus.run_track1.HActivitiesFragment.AdaptateurListeActivités
import com.lazarus.run_track1.encryptFile
import com.lazarus.run_track1.getPublicKey
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
    private lateinit var syncCloud: Button;
    private lateinit var adaptateurActivités: AdaptateurListeActivités
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
        adaptateurActivités = AdaptateurListeActivités(this.requireContext(), fileNames!!, ::enCliquéAdaptateur, ::mettreAuCloud, ::imprimer);
        vueRecyclage.layoutManager = LinearLayoutManager(this.activity);
        vueRecyclage.adapter = adaptateurActivités;
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        syncCloud = this.requireActivity().findViewById(R.id.sync_cloud);
        syncCloud.setOnClickListener{
            val fileSet = fileNames!!.toHashSet();
            listCloud()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        println("started h view");
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
        Files.list(dir).skip(0).forEach { path: Path ->
            fileNames!!.add(
                showFile(path.toFile())
            )
            Log.d("fiiiles", path.toFile().toString());
        }
    }

    private fun showFiles(files: Array<File>) {
        fileNames = ArrayList()
        for (i in 1 until files.size) {
            fileNames!!.add(files[0].name)
            Log.d("fiiiles", fileNames!![0]);
        }
    }

    private fun showFile(file: File): String {
        return file.name
    }

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
        val encryptedBytes = encryptFile(File(fileName), getPublicKey(this.requireContext()))
        //var file = Uri.fromFile(File(fileName));
        var storageRef = storage.reference;
        val fileRef = storageRef.child("gpxs/$nomFichier");
        val aesRef = storageRef.child("gpxs/aes$nomFichier");
        var uploadTask = fileRef.putBytes(encryptedBytes[0]);
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Log.d("upload__", "failed");
        }.addOnSuccessListener { taskSnapshot -> Log.d("upload__", taskSnapshot.metadata.toString())
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
        var uploadKeyTask = aesRef.putBytes(encryptedBytes[1]);
        uploadKeyTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Log.d("upload__", "failed");
        }.addOnSuccessListener { taskSnapshot -> Log.d("upload__", taskSnapshot.metadata.toString())
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
    }

    private fun imprimer(nomFichier: String){
        fileNames!!.remove(nomFichier);
        val file = File(this.context?.filesDir.toString() + "/tracks$nomFichier");
        file.delete();
    }

    private fun listCloud(){
        val listRef = storage.reference.child("/gpxs");
        listRef.listAll().addOnSuccessListener { listResult ->
            for (prefix in listResult.prefixes) {
                Log.d("preff", prefix.toString())
            }

            for (item in listResult.items) {
                // All the items under listRef.
                Log.d("fiiiles", item.toString());
            }

        }
    }

}
