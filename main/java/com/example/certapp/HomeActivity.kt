package com.example.certapp

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_home.*
import org.json.JSONObject

enum class ProviderType {
    BASIC
}

class HomeActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var usrList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Setup

        setup()
    }

    private fun setup() {

        title = "Home"

        var uid = ""

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userEmail = user.email.toString()
            emailField.text = "email: $userEmail"
            providerField.text = "uid: ${user.uid.toString()}"
            uid = user.uid.toString()
            update(uid)
            viewProfileData(uid)

        } else {
            FirebaseAuth.getInstance().signOut()
            val authIntent = Intent(this,AuthActivity::class.java)
            startActivity(authIntent)
        }

        updatebtn.setOnClickListener() {
            update(uid)
        }

        rewardsbtn.setOnClickListener() {
            val rewardsIntent = Intent(this,RewardsActivity::class.java)
            startActivity(rewardsIntent)
        }

        logOutButton.setOnClickListener(){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Something went wrong")
            builder.setMessage("Are you sure you want to log out?")
            //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                Toast.makeText(applicationContext,
                    android.R.string.yes, Toast.LENGTH_SHORT).show()
                FirebaseAuth.getInstance().signOut()
                val authIntent = Intent(this,AuthActivity::class.java)
                startActivity(authIntent)
            }

            builder.setNegativeButton("No") { _, _ ->
                Toast.makeText(applicationContext,
                    android.R.string.no, Toast.LENGTH_SHORT).show()
            }

            builder.show()

        }

        updateProfileBtn.setOnClickListener(){
            var name = nameField.text.toString()?: ""
            var age = Integer.valueOf(ageField.text.toString())?: 0
            var skinTone = skinToneField.text.toString()?: ""
            var allergies = allergiesField.text.toString()?: ""

            if( name.isNotEmpty() && age.toString().isNotEmpty() && skinTone.isNotEmpty() && allergies.isNotEmpty() ){
                dbRef = FirebaseDatabase.getInstance().getReference("Users")

                val usr = User(uid,name,age,skinTone,allergies)

                dbRef.child(uid).setValue(usr)
                    .addOnCompleteListener {
                        Log.i("HOME", "setup: profile updated correctly")

                    }.addOnFailureListener {
                        error("setup: rtdb saving failed")
                        Log.e("HOME", "setup: something went wrong with the update")
                    }
            }

        }

        clearProfileBtn.setOnClickListener(){

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmation")
            builder.setMessage("Are you sure you want to clear all profile data?")
            //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                Toast.makeText(applicationContext,
                    android.R.string.yes, Toast.LENGTH_SHORT).show()
                nameField.setText("")
                ageField.setText("")
                skinToneField.setText("")
                allergiesField.setText("")
                dbRef = FirebaseDatabase.getInstance().getReference("Users")
                dbRef.child(uid).removeValue()
            }

            builder.setNegativeButton("No") { _, _ ->
                Toast.makeText(applicationContext,
                    android.R.string.no, Toast.LENGTH_SHORT).show()
            }

            builder.show()

        }

    }

    private fun update(user: String){

        dbRef = FirebaseDatabase.getInstance().getReference("Customers")

        dbRef.child(user).child("points").get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
            providerField2.text = "Available points: ${it.value.toString()}"
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
    }

    private fun viewProfileData(user: String){

        dbRef = FirebaseDatabase.getInstance().getReference("Users")

        dbRef.child(user).get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value.toString()}")
            if(it.value!=null) {
                val data: HashMap<String, Any> = it.value as HashMap<String, Any>
                val nameVal = data["name"] as String
                val ageVal = data["age"] as Number
                val skinToneVal = data["skinTone"] as String
                val allergiesVal = data["allergies"] as String
                nameField.setText(nameVal)
                skinToneField.setText(skinToneVal)
                allergiesField.setText(allergiesVal)
                ageField.setText(String.format("%d", ageVal))
                Log.i("HOME", "setup: profile updated correctly")
            }else{
                nameField.setText("")
                ageField.setText("")
                skinToneField.setText("")
                allergiesField.setText("")
            }

        }.addOnFailureListener{
            Log.e("firebase", "setup: Error getting data", it)
        }
    }

}