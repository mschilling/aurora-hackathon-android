package com.example.dennis.hackaton

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    var usersList: ArrayList<UserModel> = ArrayList<UserModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadUsers()
        analyseTest()
        checkGps()
        getRealTimeChanges()

        //getDoc()
        mapsButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            makeUserOnline(usersList.get(5))
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        makeUserOffline(usersList.get(5))
    }

    private fun analyseTest() {
        var bundle = Bundle()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mapsButton")
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "MapsButton")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button")
        mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    private fun checkGps() {
        var lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsStatus = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        var networkStatus = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)


        var intent1 = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

        if (!gpsStatus && !networkStatus) {
            val simpleAlert = AlertDialog.Builder(this@MainActivity).create()
            simpleAlert.setTitle("Gps Location")
            simpleAlert.setMessage("Your gps location is disabled, do you want to enable it?")

            simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", { dialogInterface, i ->
                startActivity(intent1)
            })
            simpleAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "No", { dialogInterface, i ->
                Toast.makeText(applicationContext, "The application needs gps to work properly! ", Toast.LENGTH_SHORT).show()
            })
            simpleAlert.show()
        } else {
            Toast.makeText(this, "Your gps is enabled", Toast.LENGTH_LONG).show()
        }
    }

    private fun getDoc() {
        val db = FirebaseFirestore.getInstance()

        val docRef = db.collection("pointsOfInterest")
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    Log.d("Test", document.data["name"].toString() + " " + document.data["description"].toString())
                }
            } else {
                Log.d("Failed", "get failed with ", task.exception)
            }
        }
    }

    private fun getRealTimeChanges() {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("app")
        docRef.addSnapshotListener(this,
                { querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException? ->

                    for (document in querySnapshot!!.documents) {
                        Log.d("Version:", document.data.get("version").toString())
                        versionText.text = document.data.get("version").toString()
                    }
                })
    }

    fun loadUsers() {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users")

        docRef.addSnapshotListener { snapshots, ex ->
            for (doc in snapshots?.documents ?: emptyList()) {
                usersList.add(doc.toObject(UserModel::class.java))
            }

            var names = arrayOfNulls<String>(usersList.size)
            var i = 0
            for (user in usersList) {
                names[i] = user.displayName ?: ""
                i++
                Log.d("Users:", user.displayName + " " + user.status)
            }
        }
    }

    fun makeUserOnline(user: UserModel) {
        //FireStore
        var db = FirebaseFirestore.getInstance()
        var query = db.collection("users").document(user.userId ?: "")
        user.apply {
            status = "online"
        }
        query.set(user)

        //FireBase
        var fbquery = FirebaseDatabase.getInstance().getReference("status/" + user.userId).setValue("online")

        FirebaseDatabase.getInstance().getReference("/status/" + user.userId)
                .onDisconnect()     // Set up the disconnect hook
                .setValue("offline")
    }

    fun makeUserOffline(user: UserModel) {
        // Firestore
        var query = FirebaseFirestore.getInstance().collection("users").document(user.userId ?: "")
        user.apply {
            status = "offline"
        }
        query.set(user)

        // Firebase
        var fbquery = FirebaseDatabase.getInstance().getReference("status/" + user.userId).setValue("offline")
    }
}



