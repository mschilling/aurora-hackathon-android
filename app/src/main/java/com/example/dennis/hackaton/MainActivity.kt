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
import com.google.firebase.analytics.FirebaseAnalytics.Event.APP_OPEN
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    var bundle = Bundle()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        analyseTest()
        checkGps()
        getRealTimeChanges()

        //getDoc()

        mapsButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
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


}



