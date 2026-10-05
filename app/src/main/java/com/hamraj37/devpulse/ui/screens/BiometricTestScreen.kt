package com.hamraj37.devpulse.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import com.hamraj37.devpulse.util.tr
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FingerprintTestGraphic(context: Context) {
    val pm = context.packageManager
    val hasFingerprint = remember {
        try { pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) } catch (_: Throwable) { false }
    }
    val hasFace = remember {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                pm.hasSystemFeature(PackageManager.FEATURE_FACE)
            } else false
        } catch (_: Throwable) { false }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(160.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(90.dp)
                )
            }
        }

        Text(
            text = if (hasFingerprint || hasFace) "Biometric Hardware Available".tr(LocalContext.current) else "Biometric Hardware Checked".tr(LocalContext.current),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Fingerprint Sensor: " + (if (hasFingerprint) "Supported".tr(LocalContext.current) else "N/A") + " | Face Unlock: " + (if (hasFace) "Supported".tr(LocalContext.current) else "N/A"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BiometricTestScreen(
    onBack: () -> Unit,
    onPass: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    StandardTestScreen(
        title = "Biometric Sensor",
        testId = "test_fingerprint",
        onBack = onBack,
        onPass = onPass,
        onFail = onFail,
        modifier = modifier
    ) {
        FingerprintTestGraphic(context)
    }
}
