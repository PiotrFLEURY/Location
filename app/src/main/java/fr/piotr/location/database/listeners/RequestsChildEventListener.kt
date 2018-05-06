package fr.piotr.location.database.listeners

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import fr.piotr.location.database.asUid
import fr.piotr.location.database.pojos.Request
import fr.piotr.location.database.pojos.Status
import fr.piotr.location.database.userEmail
import fr.piotr.location.database.userName

const val EVENT_NEW_REQUEST = "EVENT_NEW_REQUEST"
const val EXTRA_NEW_REQUEST = "EXTRA_NEW_REQUEST"

const val EVENT_REQUEST_ACCEPTED = "EVENT_REQUEST_ACCEPTED"
const val EXTRA_REQUEST_ACCEPTED = "EXTRA_REQUEST_ACCEPTED"

class RequestsChildEventListener(private val context: Context): ChildEventListener {

    override fun onCancelled(p0: DatabaseError?) {
        //
    }

    override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
        //
    }

    override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
        val request = p0?.getValue<Request>(Request::class.java)
        onChildEvent(request!!)
        //TODO declined
    }

    override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
        val request = p0?.getValue<Request>(Request::class.java)
        onChildEvent(request!!)
    }

    override fun onChildRemoved(p0: DataSnapshot?) {
        //
    }

    fun onChildEvent(request: Request) {
        if(request.from == userName() && request.status==Status.ACCEPTED) {
            val intent = Intent(EVENT_REQUEST_ACCEPTED)
            intent.putExtra(EXTRA_REQUEST_ACCEPTED, request)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        } else if(request.target == userName() && request.status==Status.PENDING) {
            val intent = Intent(EVENT_NEW_REQUEST)
            intent.putExtra(EXTRA_NEW_REQUEST, request)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

}