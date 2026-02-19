package org.wahid.androidlocationservices

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
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
        private const val PERMISSION_REQUEST_CODE = 909;
        private const val TAG = "MainActivity"
        private const val SMS_MESSAGE_BODY =
            "Hello dear friend, my phone battery is almost die, this is my last know location updates"
        private const val SMS_MESSAGE_RECEIVER_NUMBER = "+1234567890"
    }


    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var location: MutableState<Location>


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("smsto:$SMS_MESSAGE_RECEIVER_NUMBER")
                                val addressInfo: Address = GeoCoder.getAddressInformation(this@MainActivity,location.value.longitude,location.value.latitude)
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
                                Toast.makeText(this, "No messaging app found", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                        onOpenGoogleMapsClick = {
                            val locationVal = location.value
                            openGoogleMapsDirectionBetween(
                                sourceLatitude = locationVal.latitude+0.2,
                                sourceLongitude = locationVal.longitude+0.2,
                                destinationLatitude = locationVal.latitude+0.9,
                                destinationLongitude = locationVal.longitude+0.4,
                            )
                            //TODO open google maps application
                        },
                        dateConverter = {
                            longToReadableDate(it)
                        }
                    )
                }
            }
        }
    }
    private fun openGoogleMapsDirectionBetween(
        sourceLatitude: Double,
        sourceLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double
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
    @RequiresApi(Build.VERSION_CODES.S)
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
    @RequiresApi(Build.VERSION_CODES.S)
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
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
                    if (isLocationServiceEnabled()) {
                        getLatestLocationUpdates()
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

@Composable
fun LocationScreen(
    location: Location,
    onOpenGoogleMapsClick: () -> Unit,
    onSendLocationSmsClick: () -> Unit,
    dateConverter: (Long) -> String,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(modifier = Modifier
                .fillMaxWidth(fraction = .5f)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Start,
                    text = "LONGITUDE = ${location.longitude}",
                    fontSize = 12.sp,
                    softWrap = true
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Start,
                    text = "LATITUDE = ${location.latitude}",
                    fontSize = 12.sp,
                    softWrap = true
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Start,
                    text = "TIME = ${dateConverter(location.time)}",
                    fontSize = 12.sp,
                    softWrap = true
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Start,
                    text = "PROVIDER = ${location.provider}",
                    fontSize = 12.sp,
                    softWrap = true
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Start,
                    text = "ACCURACY = ${location.accuracy}",
                    fontSize = 12.sp,
                    softWrap = true
                )


            }
        }
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {

            Button(
                onClick = onSendLocationSmsClick,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "Send SMS", textAlign = TextAlign.Center)
            }

            Button(
                onClick = onOpenGoogleMapsClick,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "Open Maps", textAlign = TextAlign.Center)
            }

        }


    }


}


@Preview
@Composable
private fun LocationScreenPreview() {
    LocationScreen(
        Location(""),
        onSendLocationSmsClick = {},
        onOpenGoogleMapsClick = {},
        dateConverter = { "" },
    )
}