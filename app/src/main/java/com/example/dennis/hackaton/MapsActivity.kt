package com.example.dennis.hackaton

import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val UPDATE_INTERVAL = (5 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    private var mLocationRequest: LocationRequest? = null
    private var latitude = 0.0
    private var longitude = 0.0
    private var myList: ArrayList<PointsOfInterest> = ArrayList<PointsOfInterest>()

    private lateinit var mGoogleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        getRealTimeChanges()
    }

    override fun onStart() {
        super.onStart()
        startLocationUpdates()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        if (mGoogleMap != null) {
            mGoogleMap!!.addMarker(MarkerOptions().position(LatLng(latitude, longitude)).title("Current Location"))

        }
    }

    protected fun startLocationUpdates() {
        mLocationRequest = LocationRequest.create()
        mLocationRequest!!.run {
            setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            setInterval(UPDATE_INTERVAL)
            setFastestInterval(FASTEST_INTERVAL)
        }

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)

        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient!!.checkLocationSettings(locationSettingsRequest)

        registerLocationListner()
    }

    private fun registerLocationListner() {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                onLocationChanged(locationResult!!.getLastLocation())
            }
        }
        if (Build.VERSION.SDK_INT >= 23 && checkPermission()) {
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper())
        }
    }

    private fun onLocationChanged(location: Location) {
        val location = LatLng(location.latitude, location.longitude)

        // show toast message with updated location
        //Toast.makeText(this,msg, Toast.LENGTH_LONG).show()

        mGoogleMap!!.clear()
        mGoogleMap!!.addMarker(MarkerOptions().position(location).title("Current Location"))
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
        getMarkersFromDatabase()
        Log.d("MyListCount", myList.count().toString())
    }

    private fun getRealTimeChanges() {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("pointsOfInterest")

        docRef.addSnapshotListener(this,
                { querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException? ->

                    for (document in querySnapshot!!.documents) {
                        var pointsOfInterest = PointsOfInterest()

                        pointsOfInterest.name = document.data["name"] as String
                        pointsOfInterest.description = document.data["description"] as String
                        pointsOfInterest.geoPoint = document.data["geoLocation"] as GeoPoint

                        myList.add(pointsOfInterest)
                        Log.d("GeoPoint", pointsOfInterest.geoPoint.toString())
                        Log.d("Name:", pointsOfInterest.name.toString())

                        // val myObject = document.toObject(MyObject::class.java)
                        // Log.e(TAG,document.data.get("foo")) // Print : "foo"
                        //Log.e(TAG, myObject.foo) // Print : ""
                    }
                    Log.d("Count", myList.count().toString())
                })
    }

    private fun getMarkersFromDatabase() {
        for (document in myList) {
            val location = LatLng(document.geoPoint.latitude, document.geoPoint.longitude)
            mGoogleMap!!.addMarker(MarkerOptions().position(location).title(document.name.toString()))
        }
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
        } else {
            requestPermissions()
            return false
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION) {
                registerLocationListner()
            }
        }
    }
}
