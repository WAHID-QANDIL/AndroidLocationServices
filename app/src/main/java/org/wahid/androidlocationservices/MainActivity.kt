package org.wahid.androidlocationservices

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.wahid.androidlocationservices.ui.theme.AndroidLocationServicesTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    companion object {
        private const val PERMISSION_REQUEST_CODE = 909
        private const val TAG = "MainActivity"
        private const val SMS_MESSAGE_BODY =
            "Hello dear friend, my phone battery is almost die, this is my last know location updates"
        private const val SMS_MESSAGE_RECEIVER_NUMBER = "+1234567890"
    }


    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var location: MutableState<Location>


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidLocationServicesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    val innerPadding = it

                    location = remember { mutableStateOf(Location("")) }

                    LocationScreen(
                        modifier = Modifier.padding(it),
                        location = location.value,
                        onSendLocationSmsClick = {
                            sendLocationSms()
                        },
                        onOpenGoogleMapsClick = {
                           openGoogleMaps()
                        },
                        dateConverter = {
                            longToReadableDate(it)
                        }
                    )
                }
            }
        }
    }

    private fun openGoogleMaps(){
        val locationVal = location.value
        openGoogleMapsDirectionBetween(
            sourceLatitude = locationVal.latitude + 0.2,
            sourceLongitude = locationVal.longitude + 0.2,
            destinationLatitude = locationVal.latitude + 0.9,
            destinationLongitude = locationVal.longitude + 0.4,
        )
    }

    private fun sendLocationSms() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$SMS_MESSAGE_RECEIVER_NUMBER")
            val addressInfo: Address = GeoCoder.getAddressInformation(
                this@MainActivity,
                location.value.longitude,
                location.value.latitude
            )
            val addressIfo = AddressIfo(
                fullAddress = addressInfo.getAddressLine(0),
                city = addressInfo.featureName,
                country = addressInfo.countryName,
                postalCode = addressInfo.postalCode,
                adminArea = addressInfo.adminArea,
            )
            Log.d(TAG, "onCreate: $addressIfo")
            putExtra(
                "sms_body",
                "$SMS_MESSAGE_BODY\n${addressIfo}"
            )
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No messaging app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGoogleMapsDirectionBetween(
        sourceLatitude: Double,
        sourceLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double,
    ) {
        val uri = Uri.parse(
            "https://maps.google.com/maps?saddr=$sourceLatitude,$sourceLongitude&daddr=$destinationLatitude,$destinationLongitude"
        )
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        startActivity(intent)
    }

    fun getLocationInfo(location: Location): LocationInfo {

        return LocationInfo(
            longitude = location.longitude,
            latitude = location.latitude,
            time = longToReadableDate(location.time),
            accuracy = location.accuracy,
            provider = location.provider ?: ""
        )

    }

    fun longToReadableDate(timeInMillis: Long): String {
        val date = Date(timeInMillis)
        val format = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())
        return format.format(date)
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStart() {
        super.onStart()
        if (isPermissionsGraded()) {
            Log.d(TAG, "onStart: PermissionsGraded ")
            if (isLocationServiceEnabled()) {
                Log.d(TAG, "onStart: LocationServiceEnabled ")
                getLatestLocationUpdates()
            } else {
                Log.d(TAG, "onStart: enableLocationService ")
                enableLocationService()
            }
        } else {
            Log.d(TAG, "onStart: requestPermissions ")
            requestPermissions()
        }

    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getLatestLocationUpdates() {
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        fusedLocation.requestLocationUpdates(
            LocationRequest.Builder(3000).apply {
                setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            }.build(),

            object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    location.value = p0.lastLocation ?: Location("")
                }
            },
            Looper.myLooper()
        )


    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: @@@@@@@@ OUTER-LOG $requestCode")
        when (requestCode) {

            PERMISSION_REQUEST_CODE -> {
                if (grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
                    if (isLocationServiceEnabled()) {
                        getLatestLocationUpdates()
                        Log.d(TAG, "onRequestPermissionsResult: @@@@@@ INNER-LOG")
                    } else {
                        enableLocationService()
                    }
                }

            }
        }

    }
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        Log.d(TAG, "onRequestPermissionsResult: OUTER-LOG $requestCode")
        when (requestCode) {

            PERMISSION_REQUEST_CODE -> {
                if (grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
                    if (isLocationServiceEnabled()) {
                        getLatestLocationUpdates()
                        Log.d(TAG, "onRequestPermissionsResult: INNER-LOG")
                    } else {
                        enableLocationService()
                    }
                }

            }
        }


    }

    fun isPermissionsGraded() = arrayOf(
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION),
        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    ).any { it == PackageManager.PERMISSION_GRANTED }


    fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_REQUEST_CODE
        )
    }


    fun enableLocationService() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    fun isLocationServiceEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val locationServices = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,

            )
        return locationServices.any { locationManager.isProviderEnabled(it) }
    }

}