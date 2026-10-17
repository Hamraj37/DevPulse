package com.hamraj37.devpulse.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.annotation.DrawableRes
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.hamraj37.devpulse.R
import java.io.File
import java.io.FileOutputStream

data class QrTypeItem(
    val name: String,
    val icon: ImageVector? = null,
    @DrawableRes val iconRes: Int? = null,
    val iconTint: Color
)

enum class LogoPosition {
    CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
}

@Composable
fun QrCodeScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val context = LocalContext.current
    var selectedTypeIndex by remember { mutableStateOf(0) }
    var selectedColorInt by remember { mutableStateOf(android.graphics.Color.BLACK) }
    var selectedBgColorInt by remember { mutableStateOf(android.graphics.Color.WHITE) }
    var bgImageUri by remember { mutableStateOf<Uri?>(null) }
    var bgImageAlpha by remember { mutableStateOf(0.75f) }
    var dotImageUri by remember { mutableStateOf<Uri?>(null) }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var logoSizeRatio by remember { mutableStateOf(0.22f) }
    var logoPosition by remember { mutableStateOf(LogoPosition.CENTER) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            bgImageUri = uri
        }
    }

    val dotImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            dotImageUri = uri
        }
    }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            logoUri = uri
        }
    }

    // State for each type
    var textContent by remember { mutableStateOf("") }
    var websiteUrl by remember { mutableStateOf("") }
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPassword by remember { mutableStateOf("") }
    var wifiSecurity by remember { mutableStateOf("WPA") }
    var emailTo by remember { mutableStateOf("") }
    var emailSubject by remember { mutableStateOf("") }
    var emailBody by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }
    var upiName by remember { mutableStateOf("") }
    var upiAmount by remember { mutableStateOf("") }
    var waPhone by remember { mutableStateOf("") }
    var waMessage by remember { mutableStateOf("") }
    var igUsername by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactOrg by remember { mutableStateOf("") }
    var contactUrl by remember { mutableStateOf("") }

    val qrTypes = remember {
        listOf(
            QrTypeItem("Text", icon = Icons.Rounded.Edit, iconTint = Color(0xFF3B82F6)),
            QrTypeItem("Website", icon = Icons.Rounded.Language, iconTint = Color(0xFF10B981)),
            QrTypeItem("WiFi", icon = Icons.Rounded.Wifi, iconTint = Color(0xFFF59E0B)),
            QrTypeItem("Email", icon = Icons.Rounded.Email, iconTint = Color(0xFFEF4444)),
            QrTypeItem("UPI", icon = Icons.Rounded.Payment, iconTint = Color(0xFF8B5CF6)),
            QrTypeItem("WhatsApp", iconRes = R.drawable.ic_whatsapp, iconTint = Color(0xFF25D366)),
            QrTypeItem("Instagram", iconRes = R.drawable.ic_instagram, iconTint = Color(0xFFE1306C)),
            QrTypeItem("Contact", icon = Icons.Rounded.Contacts, iconTint = Color(0xFFA855F7))
        )
    }

    val finalContent = remember(selectedTypeIndex, textContent, websiteUrl, wifiSsid, wifiPassword, wifiSecurity, emailTo, emailSubject, emailBody, upiId, upiName, upiAmount, waPhone, waMessage, igUsername, contactName, contactPhone, contactEmail, contactOrg, contactUrl) {
        when (selectedTypeIndex) {
            0 -> textContent.ifBlank { "Hello from DevPulse!" }
            1 -> websiteUrl.ifBlank { "https://github.com/hamraj37/DevPulse" }
            2 -> {
                val ssid = wifiSsid.ifBlank { "MyNetwork" }
                "WIFI:S:$ssid;T:$wifiSecurity;P:$wifiPassword;;"
            }
            3 -> {
                val to = emailTo.ifBlank { "skhamraj37@gmail.com" }
                val subj = emailSubject.ifBlank { "Hello" }
                "mailto:$to?subject=${Uri.encode(subj)}&body=${Uri.encode(emailBody)}"
            }
            4 -> {
                val id = upiId.ifBlank { "merchant@upi" }
                val name = upiName.ifBlank { "Merchant Name" }
                val sb = StringBuilder("upi://pay?pa=$id&pn=${Uri.encode(name)}")
                if (upiAmount.isNotBlank()) {
                    sb.append("&am=$upiAmount&cu=INR")
                }
                sb.toString()
            }
            5 -> {
                val phone = waPhone.ifBlank { "919876543210" }
                val msg = waMessage.ifBlank { "Hello from DevPulse!" }
                val encodedMsg = Uri.encode(msg)
                if (encodedMsg.isNotBlank()) {
                    "https://wa.me/$phone?text=$encodedMsg"
                } else {
                    "https://wa.me/$phone"
                }
            }
            6 -> {
                val user = igUsername.ifBlank { "hamraj37" }
                "https://instagram.com/$user"
            }
            7 -> {
                val name = contactName.ifBlank { "John Doe" }
                val phone = contactPhone.ifBlank { "+1 234 567 890" }
                val email = contactEmail.ifBlank { "john@example.com" }
                val org = contactOrg.ifBlank { "DevPulse" }
                val url = contactUrl.ifBlank { "https://github.com/hamraj37" }
                val sb = StringBuilder("BEGIN:VCARD\nVERSION:3.0\n")
                if (name.isNotBlank()) sb.append("FN:$name\n")
                if (phone.isNotBlank()) sb.append("TEL:$phone\n")
                if (email.isNotBlank()) sb.append("EMAIL:$email\n")
                if (org.isNotBlank()) sb.append("ORG:$org\n")
                if (url.isNotBlank()) sb.append("URL:$url\n")
                sb.append("END:VCARD")
                sb.toString()
            }
            else -> textContent.ifBlank { "Hello from DevPulse!" }
        }
    }

    val qrBitmap = remember(finalContent, selectedColorInt, selectedBgColorInt, bgImageUri, bgImageAlpha, dotImageUri, logoUri, logoSizeRatio, logoPosition) {
        generateCustomQrBitmap(finalContent, 512, selectedColorInt, selectedBgColorInt, bgImageUri, bgImageAlpha, dotImageUri, logoUri, logoSizeRatio, logoPosition, context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Top App Bar
        DevPulseTopAppBar(
            title = stringResource(R.string.qr_create),
            onBack = onBack,
            actions = {
                OutlinedButton(
                    onClick = {
                        if (finalContent.isBlank()) {
                            Toast.makeText(context, context.getString(R.string.toast_qr_enter_content), Toast.LENGTH_SHORT).show()
                        } else if (qrBitmap != null) {
                            val success = saveQrImageToDownloads(context, qrBitmap)
                            if (success) {
                                Toast.makeText(context, context.getString(R.string.toast_report_image_saved), Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.export_save_image),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = {
                        if (finalContent.isBlank()) {
                            Toast.makeText(context, context.getString(R.string.toast_qr_enter_content), Toast.LENGTH_SHORT).show()
                        } else if (qrBitmap != null) {
                            saveAndShareQr(context, qrBitmap)
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.qr_share),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Step 1 Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.qr_choose_type),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Grid of QR Types (2 rows of up to 4 with uniform circular icon containers)
            val chunkedQrTypes = remember { qrTypes.chunked(4) }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                chunkedQrTypes.forEach { rowTypes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowTypes.forEach { qrType ->
                            val index = qrTypes.indexOf(qrType)
                            val isSelected = selectedTypeIndex == index
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(104.dp)
                                    .clickable {
                                        selectedTypeIndex = index
                                        if (index == 2) {
                                            val connectedSsid = getConnectedSsid(context)
                                            if (connectedSsid.isNotBlank()) {
                                                wifiSsid = connectedSsid
                                            }
                                        }
                                    },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected)
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    else
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                border = if (isSelected)
                                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                else
                                    null,
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(qrType.iconTint.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (qrType.iconRes != null) {
                                            Icon(
                                                painter = painterResource(id = qrType.iconRes),
                                                contentDescription = qrType.name,
                                                tint = qrType.iconTint,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        } else if (qrType.icon != null) {
                                            Icon(
                                                imageVector = qrType.icon,
                                                contentDescription = qrType.name,
                                                tint = qrType.iconTint,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = qrType.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        repeat(4 - rowTypes.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Step 2 Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "2",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.qr_enter_content),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Enter Content Card with dynamic input fields based on selectedTypeIndex
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedTypeIndex) {
                        0 -> {
                            Text(
                                text = stringResource(R.string.qr_text_content),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = textContent,
                                onValueChange = { textContent = it },
                                placeholder = { Text(stringResource(R.string.qr_text_placeholder)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors()
                            )
                        }
                        1 -> {
                            Text(
                                text = stringResource(R.string.qr_website_url),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = websiteUrl,
                                onValueChange = { websiteUrl = it },
                                placeholder = { Text(stringResource(R.string.qr_website_placeholder)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )
                        }
                        2 -> {
                            Text(
                                text = stringResource(R.string.qr_wifi_ssid),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = wifiSsid,
                                onValueChange = { wifiSsid = it },
                                placeholder = { Text(stringResource(R.string.qr_wifi_ssid_placeholder)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.qr_wifi_pass),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = wifiPassword,
                                onValueChange = { wifiPassword = it },
                                placeholder = { Text(stringResource(R.string.qr_wifi_pass_placeholder)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.qr_wifi_security),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("WPA", "WEP", "nopass").forEach { sec ->
                                    FilterChip(
                                        selected = wifiSecurity == sec,
                                        onClick = { wifiSecurity = sec },
                                        label = { Text(sec) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        3 -> {
                            Text(
                                text = stringResource(R.string.qr_email_address),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = emailTo,
                                onValueChange = { emailTo = it },
                                placeholder = { Text(stringResource(R.string.qr_email_placeholder)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.qr_email_subject),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = emailSubject,
                                onValueChange = { emailSubject = it },
                                placeholder = { Text(stringResource(R.string.qr_email_subject_placeholder)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.qr_email_body),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = emailBody,
                                onValueChange = { emailBody = it },
                                placeholder = { Text(stringResource(R.string.qr_email_body_placeholder)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 90.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors()
                            )
                        }
                        4 -> {
                            Text(
                                text = "UPI ID (VPA)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = upiId,
                                onValueChange = { upiId = it },
                                placeholder = { Text("merchant@okaxis") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Payee Name",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = upiName,
                                onValueChange = { upiName = it },
                                placeholder = { Text("Store Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Amount (Optional)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = upiAmount,
                                onValueChange = { upiAmount = it },
                                placeholder = { Text("100.00") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )
                        }
                        5 -> {
                            Text(
                                text = "WhatsApp Phone Number (with Country Code)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = waPhone,
                                onValueChange = { waPhone = it },
                                placeholder = { Text("919876543210") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pre-filled Message (Optional)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = waMessage,
                                onValueChange = { waMessage = it },
                                placeholder = { Text("Hello!") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 90.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors()
                            )
                        }
                        6 -> {
                            Text(
                                text = "Instagram Username",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = igUsername,
                                onValueChange = { igUsername = it },
                                placeholder = { Text("username") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )
                        }
                        7 -> {
                            Text(
                                text = "Full Name",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = contactName,
                                onValueChange = { contactName = it },
                                placeholder = { Text("John Doe") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Phone Number",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = contactPhone,
                                onValueChange = { contactPhone = it },
                                placeholder = { Text("+1 234 567 890") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Email Address",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = contactEmail,
                                onValueChange = { contactEmail = it },
                                placeholder = { Text("john@example.com") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Organization / Company",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = contactOrg,
                                onValueChange = { contactOrg = it },
                                placeholder = { Text("DevPulse") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Website URL",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = contactUrl,
                                onValueChange = { contactUrl = it },
                                placeholder = { Text("https://example.com") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors(),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Step 3 / Live QR Preview Card
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "3",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.qr_live_preview),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(selectedBgColorInt))
                            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Live QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.qr_enter_content_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Dot Color Presets & Dot Image Overlay Option
                    Text(
                        text = stringResource(R.string.qr_dot_color),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val colorPresets = listOf(
                            "Black" to android.graphics.Color.BLACK,
                            "Blue" to android.graphics.Color.parseColor("#3B82F6"),
                            "Green" to android.graphics.Color.parseColor("#10B981"),
                            "Purple" to android.graphics.Color.parseColor("#8B5CF6"),
                            "Red" to android.graphics.Color.parseColor("#EF4444"),
                            "Charcoal" to android.graphics.Color.parseColor("#1F2937")
                        )
                        colorPresets.forEach { (_, colorInt) ->
                            val isSelected = dotImageUri == null && selectedColorInt == colorInt
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(colorInt), CircleShape)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        dotImageUri = null
                                        selectedColorInt = colorInt
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // QR Dot Image Overlay Option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { dotImagePickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(Icons.Rounded.Texture, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(if (dotImageUri != null) R.string.qr_change_dot_image else R.string.qr_add_dot_image))
                        }

                        if (dotImageUri != null) {
                            Button(
                                onClick = { dotImageUri = null },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Background Color Presets
                    Text(
                        text = stringResource(R.string.qr_bg_color),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val bgColorPresets = listOf(
                            "White" to android.graphics.Color.WHITE,
                            "Light Gray" to android.graphics.Color.parseColor("#F3F4F6"),
                            "Cream" to android.graphics.Color.parseColor("#FEF3C7"),
                            "Light Blue" to android.graphics.Color.parseColor("#E0F2FE"),
                            "Light Green" to android.graphics.Color.parseColor("#D1FAE5"),
                            "Light Pink" to android.graphics.Color.parseColor("#FCE7F3")
                        )
                        bgColorPresets.forEach { (_, colorInt) ->
                            val isSelected = bgImageUri == null && selectedBgColorInt == colorInt
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(colorInt), CircleShape)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        bgImageUri = null
                                        selectedBgColorInt = colorInt
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = if (colorInt == android.graphics.Color.WHITE) Color.Black else Color.DarkGray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Background Image Option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(if (bgImageUri != null) R.string.qr_change_bg_image else R.string.qr_add_bg_image))
                        }

                        if (bgImageUri != null) {
                            Button(
                                onClick = { bgImageUri = null },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Background Image Opacity Slider (if bg image is set)
                    if (bgImageUri != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.qr_bg_opacity, (bgImageAlpha * 100).toInt()),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = bgImageAlpha,
                            onValueChange = { bgImageAlpha = it },
                            valueRange = 0.1f..1.0f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Center Logo Option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { logoPickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(Icons.Rounded.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(if (logoUri != null) R.string.qr_change_logo else R.string.qr_add_logo))
                        }

                        if (logoUri != null) {
                            Button(
                                onClick = { logoUri = null },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Logo Size and Position Controls (if logo is set)
                    if (logoUri != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.qr_logo_size, (logoSizeRatio * 100).toInt()),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = logoSizeRatio,
                            onValueChange = { logoSizeRatio = it },
                            valueRange = 0.15f..0.30f,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.qr_logo_position),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                LogoPosition.CENTER to stringResource(R.string.qr_pos_center),
                                LogoPosition.TOP_LEFT to stringResource(R.string.qr_pos_top_left),
                                LogoPosition.TOP_RIGHT to stringResource(R.string.qr_pos_top_right),
                                LogoPosition.BOTTOM_LEFT to stringResource(R.string.qr_pos_bottom_left),
                                LogoPosition.BOTTOM_RIGHT to stringResource(R.string.qr_pos_bottom_right)
                            ).forEach { (pos, label) ->
                                FilterChip(
                                    selected = logoPosition == pos,
                                    onClick = { logoPosition = pos },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Save & Share Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (qrBitmap != null) {
                                    val success = saveQrImageToDownloads(context, qrBitmap)
                                    if (success) {
                                        Toast.makeText(context, context.getString(R.string.toast_report_image_saved), Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.toast_qr_share_failed, "Failed to save"), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.export_save_image), fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                        }

                        Button(
                            onClick = {
                                if (qrBitmap != null) {
                                    saveAndShareQr(context, qrBitmap)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.qr_share_image), fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)

@Suppress("DEPRECATION")
fun getConnectedSsid(context: Context): String {
    try {
        val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = connectivityManager?.activeNetwork
            val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
            if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                val wifiInfo = wifiManager?.connectionInfo
                val ssid = wifiInfo?.ssid?.removePrefix("\"")?.removeSuffix("\"")
                if (!ssid.isNullOrBlank() && ssid != "<unknown ssid>") {
                    return ssid
                }
            }
        }
    } catch (_: Throwable) {}
    return ""
}

fun generateCustomQrBitmap(
    text: String,
    size: Int,
    qrColor: Int,
    bgColor: Int,
    bgImageUri: Uri?,
    bgImageAlpha: Float,
    dotImageUri: Uri?,
    logoUri: Uri?,
    logoSizeRatio: Float,
    logoPosition: LogoPosition,
    context: Context
): Bitmap? {
    if (text.isBlank()) return null
    return try {
        val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H)
        val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (bgImageUri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(bgImageUri)
                val rawImage = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (rawImage != null) {
                    val srcRect = Rect(0, 0, rawImage.width, rawImage.height)
                    val destRect = Rect(0, 0, size, size)
                    val paint = Paint().apply {
                        isAntiAlias = true
                        isFilterBitmap = true
                        alpha = (bgImageAlpha * 255).toInt().coerceIn(0, 255)
                    }
                    canvas.drawBitmap(rawImage, srcRect, destRect, paint)
                    rawImage.recycle()
                } else {
                    canvas.drawColor(bgColor)
                }
            } catch (_: Throwable) {
                canvas.drawColor(bgColor)
            }
        } else {
            canvas.drawColor(bgColor)
        }

        val dotPaint = Paint().apply {
            color = qrColor
            isAntiAlias = true
        }

        if (dotImageUri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(dotImageUri)
                val rawDotImage = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (rawDotImage != null) {
                    val shader = BitmapShader(rawDotImage, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                    val matrix = Matrix()
                    matrix.setScale(size.toFloat() / rawDotImage.width, size.toFloat() / rawDotImage.height)
                    shader.setLocalMatrix(matrix)
                    dotPaint.shader = shader
                }
            } catch (_: Throwable) {}
        }

        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height
        val scaleX = size.toFloat() / matrixWidth
        val scaleY = size.toFloat() / matrixHeight

        // Find exact bounding box of the QR code data matrix modules
        var minX = size
        var minY = size
        var maxX = 0
        var maxY = 0
        for (x in 0 until matrixWidth) {
            for (y in 0 until matrixHeight) {
                if (bitMatrix[x, y]) {
                    val px = (x * scaleX).toInt()
                    val py = (y * scaleY).toInt()
                    if (px < minX) minX = px
                    if (py < minY) minY = py
                    if (px > maxX) maxX = px
                    if (py > maxY) maxY = px
                }
            }
        }

        for (x in 0 until matrixWidth) {
            for (y in 0 until matrixHeight) {
                if (bitMatrix[x, y]) {
                    val left = x * scaleX
                    val top = y * scaleY
                    val right = (x + 1) * scaleX
                    val bottom = (y + 1) * scaleY
                    canvas.drawRect(left, top, right, bottom, dotPaint)
                }
            }
        }

        // Draw Logo if present
        if (logoUri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(logoUri)
                val rawLogo = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (rawLogo != null) {
                    val logoSize = (size * logoSizeRatio).toInt()
                    val (left, top) = when (logoPosition) {
                        LogoPosition.CENTER -> Pair((minX + maxX - logoSize) / 2, (minY + maxY - logoSize) / 2)
                        LogoPosition.TOP_LEFT -> Pair(minX + 4, minY + 4)
                        LogoPosition.TOP_RIGHT -> Pair(maxX - logoSize - 4, minY + 4)
                        LogoPosition.BOTTOM_LEFT -> Pair(minX + 4, maxY - logoSize - 4)
                        LogoPosition.BOTTOM_RIGHT -> Pair(maxX - logoSize - 4, maxY - logoSize - 4)
                    }

                    val clampedFx = left.coerceIn(minX, maxX - logoSize)
                    val clampedFy = top.coerceIn(minY, maxY - logoSize)
                    val right = clampedFx + logoSize
                    val bottom = clampedFy + logoSize

                    val backingPaint = Paint().apply {
                        color = android.graphics.Color.WHITE
                        isAntiAlias = true
                    }
                    val cornerRadius = 16f
                    canvas.drawRoundRect(
                        RectF(clampedFx.toFloat() - 4, clampedFy.toFloat() - 4, right.toFloat() + 4, bottom.toFloat() + 4),
                        cornerRadius, cornerRadius, backingPaint
                    )

                    val srcRect = Rect(0, 0, rawLogo.width, rawLogo.height)
                    val destRect = Rect(clampedFx, clampedFy, right, bottom)
                    val logoPaint = Paint().apply {
                        isAntiAlias = true
                        isFilterBitmap = true
                    }
                    canvas.drawBitmap(rawLogo, srcRect, destRect, logoPaint)
                    rawLogo.recycle()
                }
            } catch (_: Throwable) {}
        }

        bitmap
    } catch (_: Throwable) {
        null
    }
}

fun saveQrImageToDownloads(context: Context, bitmap: Bitmap): Boolean {
    return try {
        val fileName = "DevPulse_QR_${System.currentTimeMillis()}.png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                true
            } else {
                saveQrToPublicDownloadsFile(context, bitmap, fileName)
            }
        } else {
            saveQrToPublicDownloadsFile(context, bitmap, fileName)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

private fun saveQrToPublicDownloadsFile(context: Context, bitmap: Bitmap, fileName: String): Boolean {
    return try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val file = File(downloadsDir, fileName)
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        true
    } catch (_: Exception) {
        val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        val file = File(fallbackDir, fileName)
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        true
    }
}

fun saveAndShareQr(context: Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "devpulse_qr.png")
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.qr_share_chooser_title)))
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.toast_qr_share_failed, e.message), Toast.LENGTH_SHORT).show()
    }
}
