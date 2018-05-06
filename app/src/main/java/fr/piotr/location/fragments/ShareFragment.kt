package fr.piotr.location.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import fr.piotr.location.R
import fr.piotr.location.database.pojos.Request
import fr.piotr.location.database.pojos.Status
import fr.piotr.location.database.updateRequest
import fr.piotr.location.database.userName
import fr.piotr.location.managers.LocationAppManager
import kotlinx.android.synthetic.main.fragment_share.*
import java.util.*

class ShareFragment: Fragment() {

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action== EVENT_SEND_REQUEST){
                val contactName = intent.getStringExtra(EXTRA_SEND_REQUEST_CONTACT_NAME)
                if(intent.getStringExtra(EXTRA_SEND_REQUEST_MODE)== MODE_SHARE){
                    shareWithSomebody(contactName)
                } else if(intent.getStringExtra(EXTRA_SEND_REQUEST_MODE)== MODE_REQUEST) {
                    requestLocation(contactName)
                }
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_share, container, false)
    }

    override fun onResume() {
        super.onResume()
        tv_share_with_another_app.setOnClickListener({shareLocation()})
        tv_share_with_somebody.setOnClickListener({
            val contactSelectionFragment = ContactSelectionFragment()
            contactSelectionFragment.mode = MODE_SHARE
            contactSelectionFragment.show(activity.supportFragmentManager, "ContactSelectionFragment")
        })
        tv_request_location.setOnClickListener({
            val contactSelectionFragment = ContactSelectionFragment()
            contactSelectionFragment.mode = MODE_REQUEST
            contactSelectionFragment.show(activity.supportFragmentManager, "ContactSelectionFragment")
        })

        tv_my_requests.setOnClickListener({showMyRequests()})

        LocalBroadcastManager.getInstance(activity).registerReceiver(receiver, IntentFilter(EVENT_SEND_REQUEST))
    }

    override fun onPause() {
        super.onPause()
        tv_share_with_another_app.setOnClickListener{}
        tv_share_with_somebody.setOnClickListener{}
        tv_request_location.setOnClickListener{}
        tv_my_requests.setOnClickListener{}

        LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver)
    }

    private fun shareLocation() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, LocationAppManager.getMapsUrl())
        shareIntent.type = "text/plain"
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
    }

    private fun shareWithSomebody(contactName:String) {
        updateRequest(Request(uuid = UUID.randomUUID().toString(), date = Date(), from = contactName, target = userName(), status = Status.ACCEPTED))
    }

    private fun requestLocation(contactName: String) {
        updateRequest(Request(uuid = UUID.randomUUID().toString(), date = Date(), from = userName(), target = contactName))
    }

    private fun showMyRequests() {
        MyRequestsFragment().show(activity.supportFragmentManager, "MyRequestsFragment")
    }

}
