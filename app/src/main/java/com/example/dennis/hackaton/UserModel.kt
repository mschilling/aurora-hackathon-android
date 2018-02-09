package com.example.dennis.hackaton

/**
 * Created by Dennis on 9-2-2018.
 */
data class UserModel constructor(
        var userId: String? = null,
        var displayName: String? = null,
        var photoURL: String? = null,
        var email: String? = null,
        var status: String? = null
)