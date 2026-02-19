package org.wahid.androidlocationservices

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import java.util.Locale

object GeoCoder {
    private const val TAG = "GeoCoder"

    val addressState = mutableStateOf(
        AddressIfo(
            "",
            "",
            "",
            "",
            "",
        )
    )


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getAddressInformation(
        context: Context,
        longitude: Double,
        latitude: Double,
    ): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        val address = addresses?.get(0)
        Log.d(TAG, "getAddressInformation: $address")
        return address?: Address(Locale.getDefault())
    }


}

data class AddressIfo(
    val fullAddress: String,
    val city: String,
    val country: String,
    val postalCode: String,
    val adminArea: String,
)