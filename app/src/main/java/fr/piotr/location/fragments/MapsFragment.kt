package fr.piotr.location.fragments

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fr.piotr.location.R
import fr.piotr.location.getAddress
import fr.piotr.location.managers.LocationAppManager
import kotlinx.android.synthetic.main.fragment_maps.*
import java.util.*

class MapsFragment: Fragment(), View.OnClickListener {

    override fun onClick(v: View?) {
        openGmap()
    }

    private fun openGmap() {
        val gmmIntentUri = LocationAppManager.getLocationUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        startActivity(Intent.createChooser(mapIntent, getString(R.string.maps)))
        /*mapIntent.`package` = "com.google.android.apps.maps"
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        }*/
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onResume() {
        super.onResume()

        if (LocationAppManager.currentLocation.dummy()) {
            return
        }
        val address = getAddress(Geocoder(activity, Locale.getDefault()), LocationAppManager.currentLocation)
        fragment_maps_current_location_text.text = "${address.locality} - ${address.postalCode} - ${address.countryName}"

        fragment_maps_btn.setOnClickListener(this)
    }

    override fun onPause() {
        super.onPause()
        fragment_maps_btn.setOnClickListener(null)
    }
}