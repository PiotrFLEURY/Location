package fr.piotr.location.fragments

import android.animation.Animator
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import fr.piotr.location.*
import fr.piotr.location.database.getHistory
import fr.piotr.location.listeners.MyLocationListener
import fr.piotr.location.listeners.ScollListener
import fr.piotr.location.managers.LocationAppManager.currentLocation
import kotlinx.android.synthetic.main.fragment_history.*
import java.util.*

const val EVENT_SHOW_FAB = "$TAG.EVENT_SHOW_FAB"
const val EVENT_HIDE_FAB = "$TAG.EVENT_HIDE_FAB"

class HistoryFragment: Fragment(), MyLocationListener {

    private val scrollListener = ScollListener()

    private val historyChildEventListener: ChildEventListener = object : ChildEventListener {
        override fun onCancelled(p0: DatabaseError?) {
            //
        }

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
            //
        }

        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
            onChildRemoved(p0)
            onChildAdded(p0, p1)
        }

        override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = p0?.key!!.toLong()
            val value : Coordinates = p0.getValue<Coordinates>(Coordinates::class.java)!!
            locationAdapter.addLocation(LocationEntry(calendar, value))
        }

        override fun onChildRemoved(p0: DataSnapshot?) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = p0?.key!!.toLong()
            val value : Coordinates = p0.getValue<Coordinates>(Coordinates::class.java)!!
            locationAdapter.removeLocation(LocationEntry(calendar, value))
        }

    }

    private fun buildFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(EVENT_HIDE_FAB)
        intentFilter.addAction(EVENT_SHOW_FAB)
        return intentFilter
    }

    private val filter = buildFilter()

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                EVENT_SHOW_FAB -> showFab()
                EVENT_HIDE_FAB -> hideFab()
            }
        }

    }

    private lateinit var locationAdapter: LocationAdapter

    private lateinit var history: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv_history.layoutManager = LinearLayoutManager(activity)
        rv_history.hasFixedSize()
        locationAdapter =  LocationAdapter(activity)
        rv_history.adapter = locationAdapter

    }

    override fun onResume() {
        super.onResume()
        fab_locate.setOnClickListener({ locateMe() })
        rv_history.addOnScrollListener(scrollListener)
        history = getHistory()
        history.addChildEventListener(historyChildEventListener)

        LocalBroadcastManager.getInstance(activity).registerReceiver(receiver, filter)
    }

    override fun onPause() {
        super.onPause()
        rv_history.removeOnScrollListener(scrollListener)
        fab_locate.setOnClickListener(null)
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver)
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(this)
        history.removeEventListener(historyChildEventListener)
    }

    private fun showProgress(){
        progress_locate.visibility = View.VISIBLE
    }

    private fun hideProgress(){
        progress_locate.visibility = View.INVISIBLE
    }

    private fun showDone() {
        iv_locate_done.animate().alpha(1f)
                .setDuration(400)
                .setInterpolator(DecelerateInterpolator())
                .setListener(object:Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {
                //
            }

            override fun onAnimationEnd(animation: Animator?) {
                hideDone()
            }

            override fun onAnimationCancel(animation: Animator?) {
                iv_locate_done.alpha = 0f
            }

            override fun onAnimationStart(animation: Animator?) {
                iv_locate_done.alpha = 0f
            }

        }).start()
    }

    private fun hideDone() {
        iv_locate_done.animate()
                .alpha(0f)
                .setStartDelay(2000)
                .setDuration(800)
                .setInterpolator(AccelerateInterpolator())
                .setListener(object:Animator.AnimatorListener{
                    override fun onAnimationRepeat(animation: Animator?) {
                        //
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        iv_locate_done.alpha = 0f
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        iv_locate_done.alpha = 0f
                    }

                    override fun onAnimationStart(animation: Animator?) {
                        iv_locate_done.alpha = 0f
                    }

                }).start()
    }

    private fun locateMe() {
        showProgress()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_ACCESS_FINE_LOCATION)
            return
        }

        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        val bestProvider = locationManager.getBestProvider(criteria, false)

        if(!locationManager.isProviderEnabled(bestProvider)){
            AlertDialog
                    .Builder(activity)
                    .setMessage(getString(R.string.gps_disabled_message))
                    .setPositiveButton(getString(R.string.ok), {
                        _,_ -> startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    })
                    .create()
                    .show()
            return
        }

        locationManager.requestLocationUpdates(
                bestProvider,
                0L,
                0F,
                this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_ACCESS_FINE_LOCATION -> if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) locateMe()
        }
    }

    fun hideFab() {
        fab_locate.animate().scaleX(0f).scaleY(0f).start()
    }

    fun showFab() {
        fab_locate.animate().scaleX(1f).scaleY(1f).start()
    }

    override fun fireLocationUpdate(location: Location) {
        Log.d(TAG, "Location updated")

        hideProgress()
        showDone()

        currentLocation = asCoordinates(location)

        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(this)

        history.child(Calendar.getInstance().timeInMillis.toString()).setValue(asCoordinates(location))

    }
}