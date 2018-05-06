package fr.piotr.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import fr.piotr.location.fragments.HistoryFragment
import fr.piotr.location.fragments.MapsFragment
import fr.piotr.location.fragments.ShareFragment
import fr.piotr.location.services.BackgroundService
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Context.ACTIVITY_SERVICE
import android.app.ActivityManager
import android.content.Context


const val REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 1
const val TAG = "Location"

class MainActivity : AppCompatActivity() {

    private val historyFragment = HistoryFragment()
    private val mapsFragment = MapsFragment()
    private val shareFragment = ShareFragment()

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, HistoryFragment())
                .commit()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_ACCESS_FINE_LOCATION)
            return
        } else if(!isMyServiceRunning(BackgroundService::class.java)) {
            startService(Intent(this, BackgroundService::class.java))
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== REQUEST_PERMISSION_ACCESS_FINE_LOCATION
            && grantResults.contains(PackageManager.PERMISSION_GRANTED)){
            if(!isMyServiceRunning(BackgroundService::class.java)) {
                startService(Intent(this, BackgroundService::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNavigationView.setOnNavigationItemSelectedListener { item -> onNavigationItemSelected(item) }
    }

    override fun onPause() {
        super.onPause()
        bottomNavigationView.setOnNavigationItemReselectedListener {}
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId==R.id.menu_exit){
            signOut()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun signOut() {
        stopService(Intent(this, BackgroundService::class.java))
        mAuth.signOut()
        startActivity(Intent(this, AuthenticateActivity::class.java))
        finish()
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, getFragment(item.itemId))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        return true
    }

    private fun getFragment(itemId: Int): Fragment? {
        return when(itemId){
            R.id.menu_history -> historyFragment
            R.id.menu_map -> mapsFragment
            R.id.menu_share -> shareFragment
            else -> throw IllegalArgumentException()
        }
    }

}
