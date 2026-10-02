package com.hamraj37.devpulse.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.hamraj37.devpulse.ui.MainUiState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    uiState: MainUiState = MainUiState(),
    onThemeModeChange: (String) -> Unit = {},
    onUseSystemColorsChange: (Boolean) -> Unit = {},
    onThemeColorChange: (String) -> Unit = {},
    onBack: (() -> Unit)? = null,
    onNavigateToExport: (() -> Unit)? = null,
    onNavigateToAbout: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val context = LocalContext.current

    var selectedLanguage by remember { mutableStateOf("System default") }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showDonateDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val appVersionText = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
            "${pInfo.versionName ?: "3.4.3.6"} ($versionCode)"
        } catch (_: Throwable) {
            "3.4.3.6 (332)"
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (onBack != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "App preferences & customization",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider()

        // Section 1: Theme
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 56.dp, bottom = 4.dp)
            )

            SettingsItemRow(
                icon = Icons.Rounded.NightsStay,
                title = "Theme",
                subtitle = uiState.themeMode,
                onClick = { showThemeDialog = true }
            )

            SettingsItemRow(
                icon = Icons.Rounded.Palette,
                title = "Use system colors",
                subtitle = "Match the colors from your wallpaper",
                trailing = {
                    Switch(
                        checked = uiState.useSystemColors,
                        onCheckedChange = { onUseSystemColorsChange(it) }
                    )
                }
            )

            SettingsItemRow(
                icon = Icons.Rounded.ColorLens,
                title = "Theme color",
                subtitle = if (uiState.useSystemColors) "Dynamic Material You" else uiState.themeColor,
                enabled = !uiState.useSystemColors,
                onClick = {
                    if (!uiState.useSystemColors) {
                        showColorDialog = true
                    }
                }
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Section 2: General
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "General",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 56.dp, bottom = 4.dp)
            )

            SettingsItemRow(
                icon = Icons.Rounded.Language,
                title = "Language",
                subtitle = selectedLanguage,
                onClick = { showLanguageDialog = true }
            )

            SettingsItemRow(
                icon = Icons.Rounded.Download,
                title = "Export Data",
                subtitle = "Save your device information to a text file",
                onClick = {
                    if (onNavigateToExport != null) {
                        onNavigateToExport()
                    } else {
                        Toast.makeText(context, "Opening Export Data...", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            SettingsItemRow(
                icon = Icons.Rounded.DeleteOutline,
                title = "Clear Data",
                subtitle = "Clear app's data and preferences",
                onClick = { showClearDataDialog = true }
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Section 3: Support Us
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Support Us",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 56.dp, bottom = 4.dp)
            )

            SettingsItemRow(
                icon = Icons.Rounded.Favorite,
                title = "Donate (Remove Ads)",
                subtitle = "You can show your appreciation for my work by making a small donation",
                onClick = { showDonateDialog = true }
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Section 4: About
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "About",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 56.dp, bottom = 4.dp)
            )

            SettingsItemRow(
                icon = Icons.Rounded.PrivacyTip,
                title = "Privacy Policy",
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://hamraj37.github.io/DevPulse/privacy.html"))
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Unable to open Privacy Policy URL", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            SettingsItemRow(
                icon = Icons.Rounded.Info,
                title = "App Version",
                subtitle = appVersionText,
                onClick = {
                    if (onNavigateToAbout != null) {
                        onNavigateToAbout()
                    } else {
                        Toast.makeText(context, "DevPulse $appVersionText is up to date", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Dialogs
    if (showThemeDialog) {
        val themeOptions = listOf("System default", "Light", "Dark")
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(text = "Choose Theme", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    themeOptions.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeModeChange(option)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = uiState.themeMode == option,
                                onClick = {
                                    onThemeModeChange(option)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = option)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showColorDialog) {
        val colorOptions = listOf("Blue", "Purple", "Green", "Orange", "Teal")
        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            title = { Text(text = "Choose Theme Color", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    colorOptions.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeColorChange(option)
                                    showColorDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = uiState.themeColor == option,
                                onClick = {
                                    onThemeColorChange(option)
                                    showColorDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = option)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLanguageDialog) {
        val languageOptions = listOf("System default", "English", "Spanish", "Hindi", "French", "German")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(text = "App Language", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    languageOptions.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLanguage = option
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = selectedLanguage == option,
                                onClick = {
                                    selectedLanguage = option
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = option)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text(text = "Clear Data", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to clear app cache and reset preferences?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDataDialog = false
                        Toast.makeText(context, "App data & preferences cleared", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDonateDialog) {
        AlertDialog(
            onDismissRequest = { showDonateDialog = false },
            title = { Text(text = "Donate & Support", fontWeight = FontWeight.Bold) },
            text = {
                Text("DevPulse is completely ad-free and open source! Thank you for supporting the project.")
            },
            confirmButton = {
                TextButton(onClick = { showDonateDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(text = "Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Text("DevPulse does not collect or transmit any personal user data. All hardware telemetry, app analysis, and system diagnostics are processed locally on your device.")
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && enabled) Modifier.clickable { onClick() } else Modifier
            )
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(24.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            if (!subtitle.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
        }

        if (trailing != null) {
            Spacer(modifier = Modifier.width(12.dp))
            trailing()
        }
    }
}
