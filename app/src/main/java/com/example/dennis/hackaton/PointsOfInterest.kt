package com.example.dennis.hackaton

import com.google.firebase.firestore.GeoPoint
import java.io.Serializable

/**
 * Created by Dennis on 7-2-2018.
 */

class PointsOfInterest {
    var name: String = ""
    var description: String = ""
    lateinit var geoPoint: GeoPoint

}