package fr.piotr.location.fragments

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.squareup.picasso.Picasso
import fr.piotr.location.R
import kotlinx.android.synthetic.main.fragment_user_profile.*

class UserProfileFragment:BottomSheetDialogFragment(){

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser!!

        val photoUrl = user.photoUrl.toString()
        if(!TextUtils.isEmpty(photoUrl)) {
            Picasso.with(activity).load(photoUrl).into(user_profile_iv_photo)
        }

        tv_user_profie_email.text = user.email
        et_user_profile_display_name.setText(user.displayName)

    }

    override fun onResume() {
        super.onResume()

        btn_user_profile_cancel.setOnClickListener{dismiss()}
        btn_user_profile_save.setOnClickListener{save()}
    }

    override fun onPause() {
        super.onPause()

        btn_user_profile_cancel.setOnClickListener{}
        btn_user_profile_save.setOnClickListener{}
    }

    private fun save() {
        val displayName = et_user_profile_display_name.text.toString()
        if(TextUtils.isEmpty(displayName)){
            et_user_profile_display_name.error = getString(R.string.display_name_mandatory_error)
        } else {
            val user = FirebaseAuth.getInstance().currentUser!!
            val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener({ task -> onSaveComplete(task) })
        }
    }

    private fun onSaveComplete(task:Task<Void>) {
        if(task.isSuccessful){
            Toast.makeText(activity, "Profile updated", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun changePhoto() {
        Toast.makeText(activity, "COMING SOON", Toast.LENGTH_SHORT).show()
    }
}