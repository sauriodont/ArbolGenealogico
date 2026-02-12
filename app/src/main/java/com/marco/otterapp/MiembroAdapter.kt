package com.marco.otterapp

import Miembro
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MiembroAdapter(private val miembros: List<Miembro>) :
    RecyclerView.Adapter<MiembroAdapter.MiembroViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiembroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_miembro, parent, false)
        return MiembroViewHolder(view)
    }

    override fun onBindViewHolder(holder: MiembroViewHolder, position: Int) {
        val miembro = miembros[position]
        holder.nombreText.text = miembro.nombre

        // Mostrar padre solo si existe
        if (!miembro.nombrePadre.isNullOrBlank()) {
            holder.padreText.visibility = View.VISIBLE
            holder.padreText.text = "Padre: ${miembro.nombrePadre}"
        } else {
            holder.padreText.visibility = View.GONE
        }

        // Mostrar madre solo si existe
        if (!miembro.nombreMadre.isNullOrBlank()) {
            holder.madreText.visibility = View.VISIBLE
            holder.madreText.text = "Madre: ${miembro.nombreMadre}"
        } else {
            holder.madreText.visibility = View.GONE
        }

        // Ocultar fecha de nacimiento completamente
        holder.nacimientoText.visibility = View.GONE
    }

    override fun getItemCount(): Int = miembros.size

    class MiembroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreText: TextView = itemView.findViewById(R.id.nombreText)
        val padreText: TextView = itemView.findViewById(R.id.padreText)
        val madreText: TextView = itemView.findViewById(R.id.madreText)
        val nacimientoText: TextView = itemView.findViewById(R.id.nacimientoText)
    }
}