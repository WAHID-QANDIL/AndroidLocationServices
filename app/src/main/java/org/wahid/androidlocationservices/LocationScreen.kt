package org.wahid.androidlocationservices

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


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