package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import androidx.compose.ui.res.stringResource
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun GpsTestGraphic(context: Context) {
    val locationManager = remember(context) {
        try {
            context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        } catch (_: Throwable) { null }
    }

    val hasFine = try {
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    } catch (_: Throwable) { false }

    val isGpsEnabled = try {
        locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
    } catch (_: Throwable) { false }

    val isNetworkEnabled = try {
        locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    } catch (_: Throwable) { false }

    var locationFixText by remember { mutableStateOf("Checking location provider...") }

    LaunchedEffect(hasFine, isGpsEnabled, isNetworkEnabled) {
        if (hasFine && locationManager != null) {
            try {
                var loc = if (isGpsEnabled) {
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                } else null
                if (loc == null && isNetworkEnabled) {
                    loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }
                if (loc != null) {
                    locationFixText = String.format(
                        Locale.US,
                        "Fix: Lat %.4f, Lon %.4f (Acc: %.1fm)",
                        loc.latitude, loc.longitude, loc.accuracy
                    )
                } else {
                    locationFixText = "Awaiting location satellite fix..."
                }
            } catch (_: Throwable) {
                locationFixText = "Location provider active"
            }
        } else if (!hasFine) {
            locationFixText = "Location permission requested"
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(100.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(160.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Text(
            text = if (isGpsEnabled || isNetworkEnabled) "GPS Location Hardware Active" else "Location Provider Disabled",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isGpsEnabled || isNetworkEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        Text(
            text = locationFixText,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GpsTestScreen(
    onBack: () -> Unit,
    onPass: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    StandardTestScreen(
        title = stringResource(R.string.test_item_gps_title),
        testId = "test_gps",
        onBack = onBack,
        onPass = onPass,
        onFail = onFail,
        modifier = modifier
    ) {
        GpsTestGraphic(context)
    }
}
