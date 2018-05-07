package fr.piotr.location.database

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import fr.piotr.location.database.pojos.Request

fun userEmail() = FirebaseAuth.getInstance().currentUser!!.email!!

fun userName() = FirebaseAuth.getInstance().currentUser!!.displayName!!

fun asUid(email:String = userEmail()) = email.replace("@", "_").replace(".", "_")

fun getRootNode(): DatabaseReference = FirebaseDatabase.getInstance().getReference(asUid())

fun getUsers(): DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

fun getHistory(): DatabaseReference = getRootNode().ref.child("history")

fun deleteHistory() {
    getHistory().ref.removeValue()
}

fun getSharedNode(): DatabaseReference = FirebaseDatabase.getInstance().getReference("shared")

fun getRequests(): DatabaseReference = getSharedNode().ref.child("requests")

fun getRequest(request: Request): DatabaseReference = getRequests().ref.child(request.uuid.toString())

fun updateRequest(request: Request) {
    getRequest(request).setValue(request)
}