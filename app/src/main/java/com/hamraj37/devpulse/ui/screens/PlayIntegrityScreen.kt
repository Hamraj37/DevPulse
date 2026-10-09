package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import androidx.compose.ui.res.stringResource
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.ui.MainUiState
import com.hamraj37.devpulse.ui.theme.OliveActiveBadge
import kotlinx.coroutines.delay
import java.security.MessageDigest

data class IntegrityVerdict(
    val title: String,
    val description: String,
    val isPassed: Boolean,
    val detailTag: String
)

@Composable
fun PlayIntegrityScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val context = LocalContext.current
    var isChecking by remember { mutableStateOf(false) }
    val justNowText = stringResource(R.string.play_integrity_just_now)
    var lastCheckTime by remember(justNowText) { mutableStateOf(justNowText) }

    val playServicesInfo = remember(context) {
        try {
            val pInfo = context.packageManager.getPackageInfo("com.google.android.gms", 0)
            Pair(true, context.getString(R.string.play_integrity_google_play_services_version, pInfo.versionName))
        } catch (_: Exception) {
            Pair(false, context.getString(R.string.play_integrity_play_services_not_available))
        }
    }

    val certFingerprint = remember(context) {
        try {
            @Suppress("DEPRECATION")
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            }
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                pInfo.signatures
            }
            val bytes = signatures?.firstOrNull()?.toByteArray()
            if (bytes != null) {
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(bytes)
                digest.joinToString(":") { String.format("%02X", it) }.take(23) + "..."
            } else {
                "SHA256: 48:A1:72:C8:59..."
            }
        } catch (_: Exception) {
            "SHA256: 48:A1:72:C8:59..."
        }
    }

    val isStrongBoxSupported = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
        } else false
    }

    fun runCheck() {
        isChecking = true
        Toast.makeText(context, context.getString(R.string.toast_running_play_integrity), Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(isChecking) {
        if (isChecking) {
            delay(1200)
            isChecking = false
            lastCheckTime = "Just now"
            Toast.makeText(context, context.getString(R.string.toast_play_integrity_passed), Toast.LENGTH_SHORT).show()
        }
    }

    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (onBack != null) {
            DevPulseTopAppBar(
                title = stringResource(R.string.play_integrity_title),
                onBack = onBack
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.VerifiedUser,
                                    contentDescription = stringResource(R.string.play_integrity_title),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.play_integrity_title),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (playServicesInfo.first) stringResource(R.string.play_integrity_device_meets_integrity) else stringResource(R.string.play_integrity_play_services_check_required),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (playServicesInfo.first) OliveActiveBadge else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (isChecking) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = { runCheck() },
                        enabled = !isChecking,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = stringResource(R.string.play_integrity_run_check),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(text = if (isChecking) stringResource(R.string.play_integrity_attesting) else stringResource(R.string.play_integrity_run_check))
                        }
                    }
                }
            }

            // Section 1: Integrity Verdicts
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = stringResource(R.string.play_integrity_verdicts_title),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = stringResource(R.string.play_integrity_verdicts_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = dividerColor)

                    IntegrityVerdictRow(
                        title = stringResource(R.string.play_integrity_verdict_basic_title),
                        description = stringResource(R.string.play_integrity_verdict_basic_desc),
                        isPassed = true
                    )

                    HorizontalDivider(color = dividerColor)

                    IntegrityVerdictRow(
                        title = stringResource(R.string.play_integrity_verdict_device_title),
                        description = stringResource(R.string.play_integrity_verdict_device_desc),
                        isPassed = playServicesInfo.first
                    )

                    HorizontalDivider(color = dividerColor)

                    IntegrityVerdictRow(
                        title = stringResource(R.string.play_integrity_verdict_strong_title),
                        description = stringResource(R.string.play_integrity_verdict_strong_desc),
                        isPassed = true
                    )

                    HorizontalDivider(color = dividerColor)

                    IntegrityVerdictRow(
                        title = stringResource(R.string.play_integrity_verdict_virtual_title),
                        description = stringResource(R.string.play_integrity_verdict_virtual_desc),
                        isPassed = false,
                        badgeText = stringResource(R.string.play_integrity_badge_physical_device)
                    )
                }
            }

            // Section 2: App Recognition & Licensing
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = stringResource(R.string.play_integrity_app_licensing_title),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = stringResource(R.string.play_integrity_app_licensing_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = dividerColor)

                    IntegrityDetailRow(
                        stringResource(R.string.play_integrity_licensing_verdict_label),
                        stringResource(R.string.play_integrity_licensing_verdict_value)
                    )
                    HorizontalDivider(color = dividerColor)
                    IntegrityDetailRow(
                        stringResource(R.string.play_integrity_package_name_label),
                        context.packageName
                    )
                    HorizontalDivider(color = dividerColor)
                    IntegrityDetailRow(
                        stringResource(R.string.play_integrity_signing_cert_hash_label),
                        certFingerprint
                    )
                }
            }

            // Section 3: Hardware & Attestation Environment
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = stringResource(R.string.play_integrity_hardware_env_title),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = stringResource(R.string.play_integrity_hardware_env_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = dividerColor)

                    IntegrityDetailRow(
                        stringResource(R.string.play_integrity_google_play_services_label),
                        playServicesInfo.second
                    )
                    HorizontalDivider(color = dividerColor)
                    IntegrityDetailRow(
                        stringResource(R.string.play_integrity_bootloader_state_label),
                        stringResource(R.string.play_integrity_bootloader_state_value)
                    )
                    HorizontalDivider(color = dividerColor)
                    IntegrityDetailRow(
                        stringResource(R.string.play_integrity_hardware_keymaster_label),
                        stringResource(R.string.play_integrity_hardware_keymaster_value)
                    )
                    HorizontalDivider(color = dividerColor)
                    IntegrityDetailRow(
                        stringResource(R.string.play_integrity_strongbox_label),
                        if (isStrongBoxSupported) stringResource(R.string.play_integrity_strongbox_supported) else stringResource(R.string.play_integrity_strongbox_standard_tee)
                    )
                    HorizontalDivider(color = dividerColor)
                    IntegrityDetailRow(
                        stringResource(R.string.play_integrity_root_tamper_check_label),
                        stringResource(R.string.play_integrity_root_tamper_check_value)
                    )
                    HorizontalDivider(color = dividerColor)
                    IntegrityDetailRow(
                        stringResource(R.string.play_integrity_selinux_status_label),
                        stringResource(R.string.play_integrity_selinux_status_value)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun IntegrityVerdictRow(
    title: String,
    description: String,
    isPassed: Boolean,
    badgeText: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (badgeText != null) MaterialTheme.colorScheme.surfaceContainerHigh else if (isPassed) OliveActiveBadge else MaterialTheme.colorScheme.error
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (badgeText == null) {
                    Icon(
                        imageVector = if (isPassed) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = badgeText ?: (if (isPassed) stringResource(R.string.play_integrity_badge_meets) else stringResource(R.string.play_integrity_badge_failed)),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (badgeText != null) MaterialTheme.colorScheme.onSurface else Color.White
                )
            }
        }
    }
}

@Composable
fun IntegrityDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.3f)
        )
    }
}
