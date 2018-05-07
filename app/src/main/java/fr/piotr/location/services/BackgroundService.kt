package fr.piotr.location.services

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import com.google.firebase.database.ChildEventListener
import fr.piotr.location.*
import fr.piotr.location.database.*
import fr.piotr.location.database.listeners.*
import fr.piotr.location.database.pojos.Request
import fr.piotr.location.database.pojos.Status
import fr.piotr.location.managers.LocationAppManager
import java.util.*

const val REQUEST_CHANNEL_ID = "Requests"

const val REQUEST_NOTIFICATION_ID = 1
const val REQUEST_BROADCAST_ID = 1

const val EVENT_ACCEPT_LOCATION_REQUEST = "EVENT_ACCEPT_LOCATION_REQUEST"
const val EXTRA_ACCEPT_LOCATION_REQUEST = "EXTRA_ACCEPT_LOCATION_REQUEST"

const val EVENT_ENABLE_GPS = "EVENT_ENABLE_GPS"

const val REQUEST_RESULT_BROADCAST_ID = 2
const val REQUEST_RESULT_NOTIFICATION_ID = 2

const val EVENT_OPEN_REQUEST_RESULT = "EVENT_OPEN_REQUEST_RESULT"
const val EXTRA_OPEN_REQUEST_RESULT = "EXTRA_OPEN_REQUEST_RESULT"

class BackgroundService:Service(){

    private val requests = getRequests()

    private val requestsListener:ChildEventListener = RequestsChildEventListener(this)

    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                EVENT_NEW_REQUEST -> onNewRequest(intent.getSerializableExtra(EXTRA_NEW_REQUEST) as Request)
                EVENT_REQUEST_ACCEPTED -> onRequestAccepted(intent.getSerializableExtra(EXTRA_REQUEST_ACCEPTED) as Request)
            }
        }

    }

    private val globalReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                EVENT_ACCEPT_LOCATION_REQUEST -> onAcceptLocationRequest(intent.getSerializableExtra(EXTRA_ACCEPT_LOCATION_REQUEST) as Request)
                EVENT_ENABLE_GPS -> {
                    val intent1 = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    intent1.addFlags(Intent.FLAG_FROM_BACKGROUND)
                    startActivity(intent1)
                }
                EVENT_OPEN_REQUEST_RESULT -> openGmap(intent.getSerializableExtra(EXTRA_OPEN_REQUEST_RESULT) as Request)
            }
        }

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        val filter = IntentFilter()
        filter.addAction(EVENT_NEW_REQUEST)
        filter.addAction(EVENT_REQUEST_ACCEPTED)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        val globalFilter = IntentFilter()
        globalFilter.addAction(EVENT_ACCEPT_LOCATION_REQUEST)
        globalFilter.addAction(EVENT_ENABLE_GPS)
        globalFilter.addAction(EVENT_OPEN_REQUEST_RESULT)
        applicationContext.registerReceiver(globalReceiver, globalFilter)

        requests.addChildEventListener(requestsListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        applicationContext.unregisterReceiver(globalReceiver)
        requests.removeEventListener(requestsListener)
    }

    fun onNewRequest(request:Request) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(EVENT_ACCEPT_LOCATION_REQUEST)
        intent.putExtra(EXTRA_ACCEPT_LOCATION_REQUEST, request)
        val intent2 = Intent(EVENT_ENABLE_GPS)
        val builder = NotificationCompat
                .Builder(this, REQUEST_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(getString(R.string.new_location_request_from, request.from))
                .addAction(R.drawable.ic_check_black_24dp, getString(R.string.new_request_notification_accept),
                        PendingIntent.getBroadcast(this, REQUEST_BROADCAST_ID, intent, PendingIntent.FLAG_ONE_SHOT))

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        val bestProvider = locationManager.getBestProvider(criteria, false)

        if(!locationManager.isProviderEnabled(bestProvider)){
            builder.addAction(R.drawable.ic_my_location_black_24dp, getString(R.string.enable_gps),
                    PendingIntent.getBroadcast(this, REQUEST_BROADCAST_ID, intent2, PendingIntent.FLAG_ONE_SHOT))
        }

        val notification = builder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel(REQUEST_CHANNEL_ID, REQUEST_CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT))
        }
        notificationManager.notify(REQUEST_NOTIFICATION_ID, notification)
    }

    fun onAcceptLocationRequest(request:Request) {
        request.status = Status.ACCEPTED
        locateMe(request)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(REQUEST_NOTIFICATION_ID)
    }

    fun onRequestAccepted(request: Request) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(EVENT_OPEN_REQUEST_RESULT)
        intent.putExtra(EXTRA_OPEN_REQUEST_RESULT, request)
        val notification = NotificationCompat
                .Builder(this, REQUEST_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(getString(R.string.localized, request.target))
                .addAction(R.drawable.ic_map_white_24dp, getString(R.string.open_request_result),
                        PendingIntent.getBroadcast(this, REQUEST_RESULT_BROADCAST_ID, intent, PendingIntent.FLAG_ONE_SHOT))
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel(REQUEST_CHANNEL_ID, REQUEST_CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT))
        }
        notificationManager.notify(REQUEST_RESULT_NOTIFICATION_ID, notification)
    }

    @SuppressLint("MissingPermission")
    private fun locateMe(request: Request) {

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        val bestProvider = locationManager.getBestProvider(criteria, false)

        val locationListener = object: LocationListener {
            override fun onProviderEnabled(provider: String?) {
                //
            }

            override fun onProviderDisabled(provider: String?) {
                //
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                //
            }

            override fun onLocationChanged(location: Location?) {
                locationManager.removeUpdates(this)
                LocationAppManager.currentLocation = asCoordinates(location!!)

                getHistory().child(Calendar.getInstance().timeInMillis.toString()).setValue(asCoordinates(location))

                request.coordinates = LocationAppManager.currentLocation
                updateRequest(request)
            }

        }

        locationManager.requestLocationUpdates(
                bestProvider,
                0L,
                0F,
                locationListener)
    }

    fun openGmap(request: Request) {
        val gmmIntentUri = LocationAppManager.getLocationUri(LocationAppManager.getGeoUrl(request.coordinates))
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.addFlags(Intent.FLAG_FROM_BACKGROUND)
        startActivity(Intent.createChooser(mapIntent, getString(R.string.maps)))
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(REQUEST_RESULT_NOTIFICATION_ID)

        setRequestStatus(request, Status.OPENED)
    }

}