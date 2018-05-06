package fr.piotr.location.fragments

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.content.ContextCompat.startActivity
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
import fr.piotr.location.database.getRequests
import fr.piotr.location.database.pojos.Request
import fr.piotr.location.database.userName
import fr.piotr.location.getAddress
import fr.piotr.location.managers.LocationAppManager
import kotlinx.android.synthetic.main.fragment_my_requests.*
import java.text.SimpleDateFormat

class RequestHolder(val view: View?):RecyclerView.ViewHolder(view) {

    private val sdf = SimpleDateFormat.getDateTimeInstance()

    fun bind(request: Request) {
        val tvDate = view?.findViewById<TextView>(R.id.tv_cell_request_date)
        val tvName = view?.findViewById<TextView>(R.id.tv_cell_request_name)
        val tvStatus = view?.findViewById<TextView>(R.id.tv_cell_request_status)
        val tvLocation = view?.findViewById<TextView>(R.id.tv_cell_request_location)

        tvDate?.text = sdf.format(request.date)
        tvName?.text = request.target
        tvStatus?.text = request.status.name
        if(!request.coordinates.dummy()) {
            val address = getAddress(Geocoder(view?.context), request.coordinates)
            tvLocation?.text = "${address.locality} (${address.postalCode} - ${address.countryName})"
        }

        view?.setOnClickListener({
            val gmmIntentUri = LocationAppManager.getLocationUri(LocationAppManager.getGeoUrl(request.coordinates))
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            startActivity(view.context, Intent.createChooser(mapIntent, view.context.getString(R.string.maps)), null)
        })
    }

}

class RequestsAdapter(val requests:MutableList<Request> = mutableListOf()): RecyclerView.Adapter<RequestHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RequestHolder =
            RequestHolder(LayoutInflater.from(parent?.context).inflate(R.layout.cell_request, parent, false))

    override fun getItemCount(): Int = requests.size

    override fun onBindViewHolder(holder: RequestHolder?, position: Int) {
        holder?.bind(requests[position])
    }

}

class MyRequestsFragment :BottomSheetDialogFragment() {

    lateinit var childEventListener: ChildEventListener

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_my_requests, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv_my_requests.layoutManager = LinearLayoutManager(activity)
        rv_my_requests.setHasFixedSize(true)
        val requestAdapter = RequestsAdapter()
        rv_my_requests.adapter = requestAdapter

        childEventListener = object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                //
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                //
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                onChildRemoved(p0)
                onChildAdded(p0,p1)
            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                val request = p0?.getValue(Request::class.java)!!
                if(request.from == userName()) {
                    requestAdapter.requests.add(request)
                    requestAdapter.notifyDataSetChanged()
                }
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
                val request = p0?.getValue(Request::class.java)!!
                requestAdapter.requests.remove(request)
                requestAdapter.notifyDataSetChanged()
            }

        }

    }

    override fun onResume() {
        super.onResume()
        getRequests().addChildEventListener(childEventListener)
    }

    override fun onPause() {
        super.onPause()
        getRequests().removeEventListener(childEventListener)
    }

}