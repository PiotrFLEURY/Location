package fr.piotr.location.fragments

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import fr.piotr.location.R
import fr.piotr.location.database.getUsers
import kotlinx.android.synthetic.main.fragment_contact_selection.*

typealias Mode = String

const val MODE_SHARE:Mode = "1"
const val MODE_REQUEST:Mode = "2"

const val EVENT_SEND_REQUEST = "EVENT_SEND_REQUEST"
const val EXTRA_SEND_REQUEST_CONTACT_NAME = "EXTRA_SEND_REQUEST_CONTACT_NAME"
const val EXTRA_SEND_REQUEST_MODE = "EXTRA_SEND_REQUEST_MODE"

class ContactHolder(val view: TextView?): RecyclerView.ViewHolder(view)

class ContactsAdapter(private val runnable: (contactName:String)->Unit, val contacts:MutableList<String> = mutableListOf()): RecyclerView.Adapter<ContactHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ContactHolder {
        return ContactHolder(LayoutInflater.from(parent?.context).inflate(android.R.layout.simple_list_item_1, parent, false) as TextView?)
    }

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ContactHolder?, position: Int) {
        holder?.view?.text = contacts[position]
        holder?.view?.setOnClickListener{runnable.invoke(contacts[position])}
    }
}

class ContactSelectionFragment: BottomSheetDialogFragment() {

    lateinit var mode:Mode
    private lateinit var childEventListener: ChildEventListener

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_contact_selection, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv_contacts.layoutManager = LinearLayoutManager(activity)
        rv_contacts.hasFixedSize()
        val contactsAdapter = ContactsAdapter({send(it)})
        rv_contacts.adapter = contactsAdapter

        childEventListener = object : ChildEventListener{
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
                val contact = p0?.getValue(String::class.java)!!
                contactsAdapter.contacts.add(contact)
                contactsAdapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
                val contact = p0?.getValue(String::class.java)!!
                contactsAdapter.contacts.remove(contact)
                contactsAdapter.notifyDataSetChanged()
            }

        }
    }

    override fun onResume() {
        super.onResume()
        getUsers().addChildEventListener(childEventListener)
    }

    override fun onPause() {
        super.onPause()
        getUsers().removeEventListener(childEventListener)
    }

    private fun send(contactName:String) {
        val intent = Intent(EVENT_SEND_REQUEST)
        intent.putExtra(EXTRA_SEND_REQUEST_CONTACT_NAME, contactName)
        intent.putExtra(EXTRA_SEND_REQUEST_MODE, mode)
        LocalBroadcastManager.getInstance(activity).sendBroadcast(intent)
        dismiss()
    }

}