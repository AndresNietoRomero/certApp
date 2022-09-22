package com.example.certapp

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_my_rewards.*

class MyRewardsActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_rewards)

        setup()

    }

    private fun setup(){

        title = "My Rewards"

        var uid = ""

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Log.i("Rewards", "setup: Still logged in")
            uid = user.uid.toString()
        } else {
            Log.e("Rewards", "setup: There was an issue with the login info")
            FirebaseAuth.getInstance().signOut()
            val authIntent = Intent(this,AuthActivity::class.java)
            startActivity(authIntent)
        }

        dbRef = FirebaseDatabase.getInstance().getReference("Rewards")

        dbRef.child(uid).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //Log.i("firebase", "this value: ${dataSnapshot.value}")
                for (reward in dataSnapshot.children) {
                    if(reward.value!=null) {
                        val data: HashMap<String, Any> = reward.value as HashMap<String, Any>

                        val service = data["service"] as String
                        val status = data["status"] as String

                        // creating TextView programmatically
                        val dynamicTextView = TextView(applicationContext)
                        dynamicTextView.textSize = 16f
                        dynamicTextView.text = "$service : $status"

                        // add TextView to LinearLayout
                        base_layout.addView(dynamicTextView)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("firebase", "An error occurred: $error")
            }
        })

        rewardsbtn3.setOnClickListener(){
            val rewardsIntent = Intent(this,RewardsActivity::class.java)
            startActivity(rewardsIntent)
        }

    }
}