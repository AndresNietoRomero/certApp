package com.example.certapp

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_auth.*

private const val TAG = "AuthActivity"

class AuthActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        //Setup
        setup()

        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
    }

    private fun setup() {
        title = "Authentication";

        signUpButton.setOnClickListener() {
            // check empty email and password before auth func
            if (emailAddress.text.isNotEmpty() && password.text.isNotEmpty()){
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    emailAddress.text.toString(),password.text.toString()).addOnCompleteListener {
                    if(it.isSuccessful){
                        Log.i(TAG, "setup: signed up")

                        val uid = it.result?.user?.uid

                        saveData( uid?.toString()?: "",emailAddress?.text.toString()?: "" )

                    }else{
                        Log.e(TAG, "setup: log in failed")
                        showAlert("This user already exists, try instead to log in")
                    }
                }
            }else{
                showAlert("Please type in your email and password before trying to proceed...")
            }
        }

        logInButton.setOnClickListener() {
            // check empty email and password before auth func
            if (emailAddress.text.isNotEmpty() && password.text.isNotEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    emailAddress.text.toString(),password.text.toString()).addOnCompleteListener {
                    if(it.isSuccessful){
                        Log.i(TAG, "setup: logged in")
                        showHome(emailAddress?.text.toString()?: "", ProviderType.BASIC)

                    }else{
                        Log.e(TAG, "setup: log in failed")
                        showAlert("User and password did not match our records, please try again")
                    }
                }
            }else{
                showAlert("Please type in your email and password before trying to proceed...")
            }
        }
    }

    private fun showAlert(message: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Something went wrong")
        builder.setMessage(message)
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            Toast.makeText(applicationContext,
                android.R.string.yes, Toast.LENGTH_SHORT).show()
        }

        builder.show()
    }

    private fun showHome(email: String, provider: ProviderType){

        val homeIntent = Intent(this,HomeActivity::class.java)
        startActivity(homeIntent)

    }

    private fun saveData(uid: String, email: String){

        dbRef = FirebaseDatabase.getInstance().getReference("Customers")

        val cust = Customer(uid, email, 0)

        dbRef.child(uid).setValue(cust)
            .addOnCompleteListener {
                Log.i(TAG, "setup: signed up completed")
                showHome(emailAddress?.toString()?: "", ProviderType.BASIC)
            }.addOnFailureListener {
                error("setup: rtdb saving failed")
                Log.e(TAG, "setup: rtdb saving failed")
            }
    }

}