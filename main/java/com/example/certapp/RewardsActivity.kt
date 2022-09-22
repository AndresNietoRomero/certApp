package com.example.certapp

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.logOutButton
import kotlinx.android.synthetic.main.activity_home.rewardsbtn
import kotlinx.android.synthetic.main.activity_rewards.*
import java.util.*

class RewardsActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewards)

        setup()
    }

    private fun setup() {
        title = "Rewards"

        var uid = ""

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Log.i("Rewards", "setup: Still logged in")
            val userEmail = user.email.toString()
            uid = user.uid.toString()
            getPoints(uid)
        } else {
            Log.e("Rewards", "setup: There was an issue with the login info")
            FirebaseAuth.getInstance().signOut()
            val authIntent = Intent(this,AuthActivity::class.java)
            startActivity(authIntent)
        }

        rewardsbtn.setOnClickListener() {
            killListener(uid)
            killListener(uid)
            val homeIntent = Intent(this,HomeActivity::class.java)
            startActivity(homeIntent)
        }

        myrwdbutton.setOnClickListener(){
            goToMyRewards()
        }

        logOutButton.setOnClickListener(){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Something went wrong")
            builder.setMessage("Are you sure you want to log out?")
            //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                Toast.makeText(applicationContext,
                    android.R.string.yes, Toast.LENGTH_SHORT).show()
                killListener(uid)
                killListener(uid)
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

        imageButton.setOnClickListener(){
            requestService(uid,"Eyebrow Design", 200)
        }
        rewardButton.setOnClickListener(){
            requestService(uid,"Eyebrow Design", 200)
        }
        imageButton2.setOnClickListener(){
            requestService(uid,"Lips Design", 250)
        }
        rewardButton2.setOnClickListener(){
            requestService(uid,"Lips Design", 250)
        }
        imageButton3.setOnClickListener(){
            requestService(uid,"Base Application", 450)
        }
        rewardButton3.setOnClickListener(){
            requestService(uid,"Base Application", 450)
        }
        imageButton4.setOnClickListener(){
            requestService(uid,"Eye Shadowing", 300)
        }
        rewardButton4.setOnClickListener(){
            requestService(uid,"Eye Shadowing", 300)
        }
    }

    private fun goToMyRewards(){
        val myRwdIntent = Intent(this,MyRewardsActivity::class.java)
        startActivity(myRwdIntent)
    }

    private fun requestService(uid: String, service: String, cost: Int){
            var points = 0

            dbRef.child(uid).child("points").addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.i("firebase", "Got value ${dataSnapshot.value}")
                    points = Integer.valueOf(dataSnapshot.value.toString())
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("firebase", "An error occurred: $error")
                }
            })

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Reward exchange confirmation")
            builder.setMessage("Are you sure you want to exchange $cost point for a service: $service ?")

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                if (points > cost){

                    dbRef.child(uid).child("points").setValue(points - cost)
                        .addOnCompleteListener {
                            Log.i(ContentValues.TAG, "setup: exchange completed")
                            createReward(uid, service, cost)!=null
                            Toast.makeText(
                                applicationContext,
                                "points successfully exchanged", Toast.LENGTH_SHORT
                            ).show()
                            goToMyRewards()
                        }.addOnFailureListener {
                            error("setup: rtdb saving failed")
                            Log.e(ContentValues.TAG, "setup: rtdb saving failed")
                        }

                }else{
                    Toast.makeText(applicationContext,
                        "Sorry, you don't have enough points yet", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setNegativeButton("No") { _, _ ->
                Toast.makeText(applicationContext,
                    android.R.string.no, Toast.LENGTH_SHORT).show()
            }

            builder.show()

    }

    private fun createReward(uid: String, service: String, cost: Int){
        dbRef = FirebaseDatabase.getInstance().getReference("Rewards")

        var current = Date().toString()

        val rwd = Reward(uid, service, cost, current,"Available")

        val key = dbRef.child(uid).push().key
        if (key == null) {
            Log.e(ContentValues.TAG, "Couldn't get push key for posts")
            return
        }

        val childUpdates = hashMapOf<String, Any>(
            "/$uid/$key" to rwd
        )

        dbRef.updateChildren(childUpdates)
    }

    private fun getPoints(user: String){

        dbRef = FirebaseDatabase.getInstance().getReference("Customers")
        dbRef.child(user).child("points").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.i("firebase", "Got this value ${dataSnapshot.value.toString()}")
                val pointsVal = dataSnapshot.value.toString()
                pointField.text = pointsVal
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("firebase", "An error occurred: $error")
            }
        }
        )
    }

    private fun killListener(user: String){

        dbRef.child(user).removeEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        dbRef.child(user).child("points").removeEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

    }
}