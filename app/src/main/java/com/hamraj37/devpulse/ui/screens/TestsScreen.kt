package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import androidx.compose.ui.res.stringResource
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.data.model.TestItem
import com.hamraj37.devpulse.data.model.TestStatus
import com.hamraj37.devpulse.ui.TestActivity
import com.hamraj37.devpulse.ui.theme.DevPulseTheme
import com.hamraj37.devpulse.ui.theme.OliveActiveBadge

@Composable
fun TestsScreen(
    testsList: List<TestItem>,
    onUpdateTestStatus: (String, TestStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTestingTestId by remember { mutableStateOf<String?>(null) }

    val testLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val testId = currentTestingTestId
        if (testId != null) {
            if (result.resultCode == Activity.RESULT_OK) {
                onUpdateTestStatus(testId, TestStatus.PASSED)
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                onUpdateTestStatus(testId, TestStatus.FAILED)
            }
            currentTestingTestId = null
        }
    }

    val completedCount = testsList.count { it.status == TestStatus.PASSED || it.status == TestStatus.FAILED }
    val totalCount = testsList.size.coerceAtLeast(15)
    val progressPct = completedCount.toFloat() / totalCount.toFloat()

    val automaticTests = testsList.filter { it.category == "Automatic" }
    val interactiveTests = testsList.filter { it.category == "Interactive" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.tests_hdr),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.tests_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$completedCount " + stringResource(R.string.lbl_of) + " $totalCount " + stringResource(R.string.lbl_tests_completed),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${(progressPct * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                val safeTestProgress = if (progressPct.isNaN() || progressPct.isInfinite()) 0f else progressPct.coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { safeTestProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }
        }

        // Automatic Tests Section
        if (automaticTests.isNotEmpty()) {
            Text(
                text = stringResource(R.string.tests_auto_diagnostics) + " (${automaticTests.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            automaticTests.forEach { test ->
                key(test.id) {
                    TestCardItem(
                        test = test,
                        onClick = {
                            onUpdateTestStatus(test.id, TestStatus.RUNNING)
                            onUpdateTestStatus(test.id, TestStatus.PASSED)
                            val intent = Intent(context, TestActivity::class.java).apply {
                                putExtra(TestActivity.EXTRA_TEST_ID, "automatic")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }

        // Interactive Tests Section
        Text(
            text = stringResource(R.string.tests_interactive_tests) + " (${interactiveTests.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
        )

        interactiveTests.forEach { test ->
            key(test.id) {
                TestCardItem(
                    test = test,
                    onClick = {
                        currentTestingTestId = test.id
                        onUpdateTestStatus(test.id, TestStatus.RUNNING)
                        val intent = Intent(context, TestActivity::class.java).apply {
                            putExtra(TestActivity.EXTRA_TEST_ID, test.id)
                        }
                        testLauncher.launch(intent)
                    }
                )
            }
        }
    }
}

fun getTestTitleRes(testId: String): Int {
    return when (testId) {
        "test_automatic" -> R.string.test_item_auto_title
        "test_display" -> R.string.test_item_display_title
        "test_touch" -> R.string.test_item_touch_title
        "test_flashlight" -> R.string.test_item_flashlight_title
        "test_speaker" -> R.string.test_item_speaker_title
        "test_earspeaker" -> R.string.test_item_earspeaker_title
        "test_mic" -> R.string.test_item_mic_title
        "test_proximity" -> R.string.test_item_proximity_title
        "test_light" -> R.string.test_item_light_title
        "test_accel" -> R.string.test_item_accel_title
        "test_charging" -> R.string.test_item_charging_title
        "test_vibration" -> R.string.test_item_vibration_title
        "test_bluetooth" -> R.string.test_item_bluetooth_title
        "test_fingerprint" -> R.string.test_item_fingerprint_title
        "test_gps" -> R.string.test_item_gps_title
        "test_volume" -> R.string.test_item_volume_title
        else -> 0
    }
}

fun getTestDescRes(testId: String): Int {
    return when (testId) {
        "test_automatic" -> R.string.test_item_auto_desc
        "test_display" -> R.string.test_item_display_desc
        "test_touch" -> R.string.test_item_touch_desc
        "test_flashlight" -> R.string.test_item_flashlight_desc
        "test_speaker" -> R.string.test_item_speaker_desc
        "test_earspeaker" -> R.string.test_item_earspeaker_desc
        "test_mic" -> R.string.test_item_mic_desc
        "test_proximity" -> R.string.test_item_proximity_desc
        "test_light" -> R.string.test_item_light_desc
        "test_accel" -> R.string.test_item_accel_desc
        "test_charging" -> R.string.test_item_charging_desc
        "test_vibration" -> R.string.test_item_vibration_desc
        "test_bluetooth" -> R.string.test_item_bluetooth_desc
        "test_fingerprint" -> R.string.test_item_fingerprint_desc
        "test_gps" -> R.string.test_item_gps_desc
        "test_volume" -> R.string.test_item_volume_desc
        else -> 0
    }
}

@Composable
fun TestCardItem(
    test: TestItem,
    onClick: () -> Unit
) {
    val titleRes = getTestTitleRes(test.id)
    val descRes = getTestDescRes(test.id)

    val itemTitle = if (titleRes != 0) stringResource(titleRes) else test.title
    val itemDesc = if (descRes != 0) stringResource(descRes) else test.description

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (test.status) {
                        TestStatus.PASSED -> OliveActiveBadge.copy(alpha = 0.15f)
                        TestStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (test.status) {
                                TestStatus.PASSED -> Icons.Rounded.CheckCircle
                                TestStatus.FAILED -> Icons.Rounded.Close
                                else -> Icons.Rounded.Construction
                            },
                            contentDescription = null,
                            tint = when (test.status) {
                                TestStatus.PASSED -> OliveActiveBadge
                                TestStatus.FAILED -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = itemTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = itemDesc,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (test.status) {
                    TestStatus.PASSED -> OliveActiveBadge
                    TestStatus.FAILED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                }
            ) {
                Text(
                    text = when (test.status) {
                        TestStatus.PASSED -> stringResource(R.string.lbl_passed)
                        TestStatus.FAILED -> stringResource(R.string.lbl_failed)
                        else -> stringResource(R.string.lbl_test)
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = when (test.status) {
                        TestStatus.PASSED -> Color.White
                        TestStatus.FAILED -> Color.White
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestsScreenPreview() {
    DevPulseTheme {
        TestsScreen(
            testsList = listOf(
                TestItem("screen", "Display Touch", "Interactive", "Test multi-touch and display digitizer", TestStatus.NOT_TESTED),
                TestItem("vibration", "Vibration Motor", "Interactive", "Verify haptic feedback motor", TestStatus.PASSED)
            ),
            onUpdateTestStatus = { _, _ -> }
        )
    }
}
