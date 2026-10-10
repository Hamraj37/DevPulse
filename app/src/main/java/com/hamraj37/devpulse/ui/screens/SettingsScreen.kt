package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.stringResource
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
    onThemePaletteChange: (String) -> Unit = {},
    onMonetToggle: (Boolean) -> Unit = {},
    onBack: (() -> Unit)? = null,
    onNavigateToExport: (() -> Unit)? = null,
    onNavigateToAbout: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val context = LocalContext.current

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showThemePaletteDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showDonateDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val systemDefaultLabel = stringResource(R.string.lbl_system_default)
    val supportedLanguages = remember(systemDefaultLabel) {
        listOf(
            "system" to systemDefaultLabel,
            "en" to "English",
            "ru" to "Русский",
            "hi" to "हिन्दी",
            "bn" to "বাংলা",
            "zh" to "中文 (简体)"
        )
    }

    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentLangCode = if (currentLocales.isEmpty) {
        "system"
    } else {
        currentLocales.get(0)?.language ?: "system"
    }
    val currentLanguageDisplayName = supportedLanguages.find {
        it.first == currentLangCode || (it.first != "system" && currentLangCode.startsWith(it.first))
    }?.second ?: systemDefaultLabel

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
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (onBack != null) {
            DevPulseTopAppBar(
                title = stringResource(R.string.settings_title),
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
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = stringResource(R.string.settings_preferences_customization),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider()

        // Section 1: Theme
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.lbl_theme),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 56.dp, bottom = 4.dp)
            )

            SettingsItemRow(
                icon = Icons.Rounded.NightsStay,
                title = stringResource(R.string.lbl_theme),
                subtitle = uiState.themeMode,
                onClick = { showThemeDialog = true }
            )

            SettingsItemRow(
                icon = Icons.Rounded.ColorLens,
                title = stringResource(R.string.settings_dynamic_material_you),
                subtitle = stringResource(R.string.settings_match_wallpaper_colors),
                trailing = {
                    Switch(
                        checked = uiState.isMonetEnabled,
                        onCheckedChange = { onMonetToggle(it) }
                    )
                },
                onClick = { onMonetToggle(!uiState.isMonetEnabled) }
            )

            SettingsItemRow(
                icon = Icons.Rounded.Palette,
                title = stringResource(R.string.settings_theme_color),
                subtitle = when (uiState.themePalette) {
                    "Monet", "Dynamic", "Monet (Dynamic)" -> "Monet (Dynamic)"
                    "Olive" -> "Olive (Default)"
                    else -> uiState.themePalette
                },
                onClick = { showThemePaletteDialog = true }
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Section 2: General
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.lbl_general),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 56.dp, bottom = 4.dp)
            )

            SettingsItemRow(
                icon = Icons.Rounded.Language,
                title = stringResource(R.string.settings_choose_app_language),
                subtitle = currentLanguageDisplayName,
                onClick = { showLanguageDialog = true }
            )

            SettingsItemRow(
                icon = Icons.Rounded.Download,
                title = stringResource(R.string.lbl_export_data),
                subtitle = stringResource(R.string.lbl_export_data_desc),
                onClick = {
                    if (onNavigateToExport != null) {
                        onNavigateToExport()
                    } else {
                        Toast.makeText(context, context.getString(R.string.toast_opening_export_data), Toast.LENGTH_SHORT).show()
                    }
                }
            )

            SettingsItemRow(
                icon = Icons.Rounded.DeleteOutline,
                title = stringResource(R.string.lbl_clear_data),
                subtitle = stringResource(R.string.lbl_clear_data_desc),
                onClick = { showClearDataDialog = true }
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Section 3: Support Us
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.btn_donate),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 56.dp, bottom = 4.dp)
            )

            SettingsItemRow(
                icon = Icons.Rounded.Favorite,
                title = stringResource(R.string.btn_donate),
                subtitle = stringResource(R.string.settings_donate_msg),
                onClick = { showDonateDialog = true }
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Section 4: About
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.about_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 56.dp, bottom = 4.dp)
            )

            SettingsItemRow(
                icon = Icons.Rounded.PrivacyTip,
                title = stringResource(R.string.lbl_privacy_policy),
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://hamraj37.github.io/DevPulse/privacy.html"))
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, context.getString(R.string.toast_unable_open_privacy_policy), Toast.LENGTH_SHORT).show()
                    }
                }
            )

            SettingsItemRow(
                icon = Icons.Rounded.Info,
                title = stringResource(R.string.lbl_version),
                subtitle = appVersionText,
                onClick = {
                    if (onNavigateToAbout != null) {
                        onNavigateToAbout()
                    } else {
                        Toast.makeText(context, context.getString(R.string.toast_app_up_to_date, appVersionText), Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialogs
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(text = stringResource(R.string.settings_choose_app_language), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    supportedLanguages.forEach { (code, name) ->
                        val isSelected = currentLangCode == code || (code != "system" && currentLangCode.startsWith(code))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showLanguageDialog = false
                                    val localeList = if (code == "system") {
                                        LocaleListCompat.getEmptyLocaleList()
                                    } else {
                                        LocaleListCompat.forLanguageTags(code)
                                    }
                                    AppCompatDelegate.setApplicationLocales(localeList)
                                    (context as? Activity)?.recreate()
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    showLanguageDialog = false
                                    val localeList = if (code == "system") {
                                        LocaleListCompat.getEmptyLocaleList()
                                    } else {
                                        LocaleListCompat.forLanguageTags(code)
                                    }
                                    AppCompatDelegate.setApplicationLocales(localeList)
                                    (context as? Activity)?.recreate()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (showThemeDialog) {
        val themeOptions = listOf("System default", "Light", "Dark")
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(text = stringResource(R.string.dialog_choose_theme), fontWeight = FontWeight.Bold) },
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
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (showThemePaletteDialog) {
        val paletteOptions = listOf(
            "Monet" to ("Monet (Dynamic)" to androidx.compose.ui.graphics.Color(0xFF829827)),
            "Red" to ("Red" to androidx.compose.ui.graphics.Color(0xFFB3261E)),
            "Blue" to ("Blue" to androidx.compose.ui.graphics.Color(0xFF1B62B2)),
            "Green" to ("Green" to androidx.compose.ui.graphics.Color(0xFF2D6A4F)),
            "Yellow" to ("Yellow" to androidx.compose.ui.graphics.Color(0xFF755B00)),
            "Olive" to ("Olive (Default)" to androidx.compose.ui.graphics.Color(0xFF6B8E23))
        )
        AlertDialog(
            onDismissRequest = { showThemePaletteDialog = false },
            title = { Text(text = stringResource(R.string.dialog_choose_theme_color), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    paletteOptions.forEach { (key, info) ->
                        val (label, color) = info
                        val isSelected = uiState.themePalette == key || (key == "Monet" && (uiState.themePalette == "Dynamic" || uiState.themePalette == "Monet (Dynamic)"))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemePaletteChange(key)
                                    showThemePaletteDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onThemePaletteChange(key)
                                    showThemePaletteDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                modifier = Modifier.size(20.dp),
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = color
                            ) {}
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemePaletteDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text(text = stringResource(R.string.lbl_clear_data), fontWeight = FontWeight.Bold) },
            text = {
                Text(stringResource(R.string.settings_clear_data_confirm_msg))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDataDialog = false
                        context.getSharedPreferences("devpulse_prefs", Context.MODE_PRIVATE).edit().clear().apply()
                        context.getSharedPreferences("devpulse_test_prefs", Context.MODE_PRIVATE).edit().clear().apply()
                        Toast.makeText(context, context.getString(R.string.toast_app_data_cleared), Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(stringResource(R.string.btn_clear))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (showDonateDialog) {
        DonateDialog(onDismissRequest = { showDonateDialog = false })
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(text = stringResource(R.string.lbl_privacy_policy), fontWeight = FontWeight.Bold) },
            text = {
                Text(stringResource(R.string.settings_privacy_policy_desc))
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text(stringResource(R.string.btn_close))
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
