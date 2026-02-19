package org.wahid.androidlocationservices

data class LocationInfo(
    val longitude: Double,
    val latitude: Double,
    val time:String,
    val provider:String,
    val accuracy: Float,
)
