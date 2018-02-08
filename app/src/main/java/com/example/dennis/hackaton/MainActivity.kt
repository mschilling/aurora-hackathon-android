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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var mFireStore: FirebaseFirestore? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    var myList: MutableList<PointsOfInterest> = mutableListOf<PointsOfInterest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkGps()
        getDataFromFirestore()
        //getDoc()
        getRealTimeChanges()

        mapsButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }


    private fun checkGps() {
        var lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsStatus = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        var intent1 = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

        if (!gpsStatus) {
            val simpleAlert = AlertDialog.Builder(this@MainActivity).create()
            simpleAlert.setTitle("Gps Location")
            simpleAlert.setMessage("Your gps location is disabled, do you want to enable it?")

            simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", { dialogInterface, i ->
                startActivity(intent1)
            })
            simpleAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "No", { dialogInterface, i ->
                Toast.makeText(applicationContext, "You clicked on No", Toast.LENGTH_SHORT).show()
            })
            simpleAlert.show()
        } else {
            Toast.makeText(this, "Your gps is enabled", Toast.LENGTH_LONG).show()
        }
    }

    private fun getDataFromFirestore() {
        mFireStore = FirebaseFirestore.getInstance()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        mFireStore!!.collection("pointsOfInterest")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            var pointsOfInterest = PointsOfInterest()

                            pointsOfInterest.name = document.data["name"] as String

                            myList.add(pointsOfInterest)

                            Log.d("Test", document.data["name"].toString() + " " + document.data["description"].toString())
                            Log.d("Adding", myList.count().toString())
                        }
                    } else {
                        Log.w("Test", "Error getting documents.", task.exception)
                    }
                }
    }

    fun getDoc() {
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

    fun getRealTimeChanges() {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("app")

        docRef.addSnapshotListener(this,
                { querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException? ->

                    for (document in querySnapshot!!.documents) {
                        Log.d("Version:", document.data.get("version").toString())

                        versionText.text = document.data.get("version").toString()
                        // val myObject = document.toObject(MyObject::class.java)
                        // Log.e(TAG,document.data.get("foo")) // Print : "foo"
                        //Log.e(TAG, myObject.foo) // Print : ""
                    }
                })
    }


}



