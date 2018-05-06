package fr.piotr.location.managers

import android.net.Uri
import fr.piotr.location.Coordinates

object LocationAppManager {

    var currentLocation: Coordinates = Coordinates()

    fun getLocationUri(url:String = getGeoUrl()):Uri = Uri.parse(url)

    fun getGeoUrl(coordinates: Coordinates = currentLocation) = "geo:${coordinates.latitude},${coordinates.longitude}"

    fun getMapsUrl() = "https://www.google.com/maps/search/?api=1&query=${currentLocation.latitude},${currentLocation.longitude}"

}
