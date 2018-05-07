package fr.piotr.location

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import fr.piotr.location.database.asUid
import fr.piotr.location.database.getUsers
import fr.piotr.location.database.pojos.Contact
import fr.piotr.location.fragments.UserProfileFragment
import kotlinx.android.synthetic.main.activity_authenticate.*


const val RC_SIGN_IN = 1

class AuthenticateActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticate)

        mAuth = FirebaseAuth.getInstance()

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

    }

    override fun onResume() {
        super.onResume()
        google_sign_in_button.setOnClickListener { atemptLogin() }
        btn_create_account.setOnClickListener{createAccountWithEmailPassword()}
        btn_email_password_sign_in.setOnClickListener{signInWithEmailPassword()}
        et_password_login.setOnEditorActionListener { _, _, _ -> signInWithEmailPassword(); true }

        if(mAuth.currentUser!=null){
            onConnectionSuccess()
        }
    }

    override fun onPause() {
        super.onPause()
        google_sign_in_button.setOnClickListener{}
        btn_create_account.setOnClickListener{}
        btn_email_password_sign_in.setOnClickListener{}
    }

    private fun atemptLogin() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleSignInResult(result)
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess)
        if (result.isSuccess) {
            // Signed in successfully, show authenticated UI.
            val acct = result.signInAccount
            firebaseAuthWithGoogle(acct!!)

        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        onConnectionSuccess()

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        Toast.makeText(this@AuthenticateActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                    }

                    // ...
                }
    }

    private fun onConnectionSuccess() {
        val user = mAuth.currentUser
        if(TextUtils.isEmpty(user?.displayName)){
            UserProfileFragment().show(supportFragmentManager, "userProfileFragment")
        } else {
            getUsers().child(asUid()).setValue(Contact(email = user?.email, displayName = user?.displayName!!, photoUrl = user.photoUrl?.toString()))
            Toast.makeText(this, getString(R.string.welcome_user, user?.displayName), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(this, getString(R.string.connection_failed), Toast.LENGTH_LONG).show()
    }

    private fun getEmail() = et_email_login.text.toString()

    private fun getPawword() = et_password_login.text.toString()

    private fun formValid(): Boolean {
        if(TextUtils.isEmpty(getEmail())){
            et_email_login.error = getString(R.string.email_empty_error)
            return false
        } else {
            et_email_login.error = null
        }
        if(TextUtils.isEmpty(getPawword())){
            et_password_login.error = getString(R.string.password_empty_error)
            return false
        } else {
            et_password_login.error = null
        }
        return true
    }

    private fun signInWithEmailPassword() {
        if(formValid()) {
            mAuth.signInWithEmailAndPassword(getEmail(), getPawword())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            onConnectionSuccess()
                        } else {
                            Toast.makeText(this, getString(R.string.connection_failed), Toast.LENGTH_LONG).show()
                        }

                    }
        }
    }

    private fun createAccountWithEmailPassword() {
        if(formValid()) {
            mAuth.createUserWithEmailAndPassword(getEmail(), getPawword())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            onConnectionSuccess()
                        } else {
                            val message = task.exception?.message?: getString(R.string.connection_failed)
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                        }
                    }
        }
    }
}
