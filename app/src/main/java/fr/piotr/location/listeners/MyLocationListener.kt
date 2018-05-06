package fr.piotr.location.listeners

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

interface MyLocationListener : LocationListener {

    fun fireLocationUpdate(location: Location)

    override fun onLocationChanged(location: Location?) {
        location?.let { fireLocationUpdate(location) }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

}