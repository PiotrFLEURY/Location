package fr.piotr.location

import android.content.Context
import android.location.Geocoder
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class LocationHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val dateFormat = SimpleDateFormat.getDateTimeInstance()

    private val tvLocality:TextView = itemView.findViewById(R.id.cell_location_locality)
    private val tvPostalCode:TextView = itemView.findViewById(R.id.cell_location_postal_code)
    private val tvCountry:TextView = itemView.findViewById(R.id.cell_location_country)
    private val driveLineTop:View = itemView.findViewById(R.id.cell_location_drive_line_top)
    private val driveLineBottom:View = itemView.findViewById(R.id.cell_location_drive_line_bottom)
    private val tvDate:TextView = itemView.findViewById(R.id.cell_location_date)

    fun setText(locality:String, postalCode:String, country:String){
        tvLocality.text=locality
        tvPostalCode.text=postalCode
        tvCountry.text=country
    }

    fun setTop(top:Boolean) {
        driveLineTop.visibility = when(top) { true -> View.INVISIBLE else -> View.VISIBLE }
    }

    fun setBottom(bottom:Boolean) {
        driveLineBottom.visibility = when(bottom) { true -> View.INVISIBLE else -> View.VISIBLE }
    }

    fun setDate(calendar: Calendar){
        tvDate.text = dateFormat.format(calendar.time)
    }

}

class LocationAdapter(context: Context):RecyclerView.Adapter<LocationHolder>() {

    private val geoCoder = Geocoder(context)
    private val locationsHistory:MutableList<LocationEntry> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): LocationHolder {
        return LocationHolder(LayoutInflater.from(parent?.context).inflate(R.layout.cell_location, parent, false))
    }

    override fun getItemCount(): Int {
        return locationsHistory.size
    }

    override fun onBindViewHolder(holder: LocationHolder?, position: Int) {
        val address = getAddress(geoCoder, getItem(position).coordinates)
        holder?.setText(address.locality, address.postalCode, address.countryName)
        holder?.setTop(position==0)
        holder?.setBottom(position==itemCount-1)
        holder?.setDate(getItem(position).calendar)
    }

    private fun getItem(position: Int): LocationEntry {
        return locationsHistory[locationsHistory.size - position -1]
    }

    fun addLocation(location: LocationEntry){
        locationsHistory.add(location)
        notifyDataSetChanged()
    }

    fun removeLocation(locationEntry: LocationEntry) {
        locationsHistory.remove(locationEntry)
        notifyDataSetChanged()
    }

}