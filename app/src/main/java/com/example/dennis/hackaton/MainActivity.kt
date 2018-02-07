package com.example.dennis.hackaton

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {
    private var mFireStore: FirebaseFirestore? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkGps()
        connectToFireStore()


        mapsButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkGps() {
        var lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsStatus = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        var intent1 = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

        if(!gpsStatus) {
            val simpleAlert = AlertDialog.Builder(this@MainActivity).create()
            simpleAlert.setTitle("Gps Location")
            simpleAlert.setMessage("Your gps location is disabled, do you want to enable it?")

            simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", {
                dialogInterface, i ->
                startActivity(intent1)
            })
            simpleAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "No", {
                dialogInterface, i ->
                Toast.makeText(applicationContext, "You clicked on No", Toast.LENGTH_SHORT).show()
            })
            simpleAlert.show()
        }
        else {
            Toast.makeText(this, "Your gps is already enabled", Toast.LENGTH_LONG).show()
        }
    }
    private fun connectToFireStore() {

        mFireStore = FirebaseFirestore.getInstance()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        mFireStore!!.collection("pointsOfInterest")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            Log.d("Test", document.data["name"].toString() + " " + document.data["description"].toString())
                        }
                    } else {
                        Log.w("Test", "Error getting documents.", task.exception)
                    }
                }
    }
}
