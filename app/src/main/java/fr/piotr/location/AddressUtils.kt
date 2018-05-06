package fr.piotr.location

import android.location.Address
import android.location.Geocoder
import android.location.Location
import java.io.Serializable
import java.util.*

fun getAddress(geocoder: Geocoder, coordinates:Coordinates): Address = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)[0]

fun asCoordinates(location: Location):Coordinates = Coordinates(location.latitude, location.longitude)

data class Coordinates(val latitude:Double=0.toDouble(), val longitude:Double=0.toDouble()):Serializable {
    fun dummy() = latitude.toInt()+longitude.toInt()==0
}

data class LocationEntry(val calendar: Calendar = Calendar.getInstance(), val coordinates: Coordinates)