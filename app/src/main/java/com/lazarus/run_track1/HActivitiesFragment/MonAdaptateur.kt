package com.lazarus.run_track1.HActivitiesFragment

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.lazarus.run_track1.R

class AdaptateurListeActivités (private val context: Context, private val ensembleDonnées: ArrayList<String>,
            private val enCliqué: (String) -> Unit) :
        RecyclerView.Adapter<AdaptateurListeActivités.PorteVueActivité>() {

    private lateinit var activityMapFragment:ActivityMapFragment;

    interface EnInfoActivitéClicquéÉcouteur {
        fun enInfoActivitéClicqué(nomFichier: String)
    }

    inner class PorteVueActivité(itemView: View, val enCliqué: (String) -> Unit,
             private val écouteur:EnInfoActivitéClicquéÉcouteur = HActivityFragment()) : RecyclerView.ViewHolder(itemView) {
        private val lActivité: AppCompatButton = itemView.findViewById(R.id.une_activité);
        private val infoActivité:RelativeLayout = itemView.findViewById(R.id.info_d_activité)
        private var nomActivité:String? = null
        init {
            lActivité.setOnClickListener {
                println("clicked!");
                if (infoActivité.visibility == View.GONE){
                    infoActivité.visibility = View.VISIBLE;
                }else{
                    infoActivité.visibility = View.GONE;
                }
            }
            infoActivité.setOnClickListener{
                nomActivité?.let{
                    enCliqué(it)
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
        return PorteVueActivité(vue, enCliqué);
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