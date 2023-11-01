package com.lazarus.run_track1.HActivitiesFragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.lazarus.run_track1.R


class AdaptateurListeActivités (private val context: Context, private val ensembleDonnées: ArrayList<String>,
            private val enCliqué: (String) -> Unit, private val mettreAuCloud: (String) -> Unit, private val imprimer: (String) -> Unit) :
        RecyclerView.Adapter<AdaptateurListeActivités.PorteVueActivité>() {

    private lateinit var activityMapFragment:ActivityMapFragment;

    interface EnInfoActivitéClicquéÉcouteur {
        fun enInfoActivitéClicqué(nomFichier: String)
    }

    inner class PorteVueActivité(itemView: View, val enCliqué: (String) -> Unit, val mettreAuCloud: (String) -> Unit, val imprimer: (String) ->Unit,
             private val écouteur:EnInfoActivitéClicquéÉcouteur = HActivityFragment()) : RecyclerView.ViewHolder(itemView) {
        private val lActivité: AppCompatButton = itemView.findViewById(R.id.une_activité);
        private val infoActivité:ConstraintLayout= itemView.findViewById(R.id.info_d_activité)
        private val uploadCloud:RelativeLayout = itemView.findViewById(R.id.upload_cloud);
        private val voirActivité:RelativeLayout = itemView.findViewById(R.id.voir_activité);
        private val trackSettingsIcon: ImageView = itemView.findViewById(R.id.track_settings_icon)
        private val trackSettingsDropdown: ConstraintLayout = itemView.findViewById(R.id.track_settings_dropdown)
        private var nomActivité:String? = null
        init {
            lActivité.setOnClickListener {
                println("clicked!");
                if (infoActivité.visibility == View.GONE){
                    infoActivité.visibility = View.VISIBLE;
                    uploadCloud.visibility = View.VISIBLE
                    voirActivité.visibility = View.VISIBLE
                }else{
                    infoActivité.visibility = View.GONE;
                    uploadCloud.visibility = View.GONE;
                    voirActivité.visibility = View.GONE;
                }
            }
            trackSettingsIcon.setOnClickListener{
                if (trackSettingsDropdown.visibility == View.GONE) {
                    trackSettingsDropdown.visibility = View.VISIBLE
                } else {
                    trackSettingsDropdown.visibility = View.GONE
                }
            }
            trackSettingsDropdown.setOnClickListener {
                nomActivité?.let{
                    this@AdaptateurListeActivités.notifyItemRemoved(this.adapterPosition);
                    imprimer(it)
                }
            }
            voirActivité.setOnClickListener{
                nomActivité?.let{
                    enCliqué(it)
                }
            }
            uploadCloud.setOnClickListener {
                nomActivité?.let {
                    mettreAuCloud(it);
                }
            }

        }

        fun attache(nomFichier:String){
            lActivité.text = nomFichier;
            nomActivité = nomFichier;
        }

        fun cliquerVoirActivité(nomFichier: String){
            //enInfoActivitéClicqué(nomFichier);
            //écouteur.enInfoActivitéClicqué(nomFichier);
        }
    }


    //La prochaine fois, je créerai une nouvelle classe qui mette en œuvre ces méthodes en français
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PorteVueActivité {
        val vue = LayoutInflater.from(context)
            .inflate(R.layout.objet_activite, parent, false);
        return PorteVueActivité(vue, enCliqué, mettreAuCloud, imprimer);
    }

    override fun onBindViewHolder(holder: PorteVueActivité, position: Int) {
        val donée = ensembleDonnées[position];
        holder.attache(donée);
        holder.cliquerVoirActivité(donée);
    }

    override fun getItemCount(): Int {
        return ensembleDonnées.size;
    }

    /*private fun changerÉtatObjet(position: Int) {
        étatsOuvert[position] = !étatsOuvert[position]
        notifyItemChanged(position)
    }*/


}