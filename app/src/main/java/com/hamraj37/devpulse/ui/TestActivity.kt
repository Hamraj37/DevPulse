package com.hamraj37.devpulse.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.hamraj37.devpulse.ui.screens.AccelerometerTestScreen
import com.hamraj37.devpulse.ui.screens.AutomaticTestsScreen
import com.hamraj37.devpulse.ui.screens.BiometricTestScreen
import com.hamraj37.devpulse.ui.screens.BluetoothTestScreen
import com.hamraj37.devpulse.ui.screens.ChargingTestScreen
import com.hamraj37.devpulse.ui.screens.DisplayTestScreen
import com.hamraj37.devpulse.ui.screens.EarSpeakerTestScreen
import com.hamraj37.devpulse.ui.screens.FlashlightTestScreen
import com.hamraj37.devpulse.ui.screens.GpsTestScreen
import com.hamraj37.devpulse.ui.screens.LightSensorTestScreen
import com.hamraj37.devpulse.ui.screens.MicTestScreen
import com.hamraj37.devpulse.ui.screens.MultitouchTestScreen
import com.hamraj37.devpulse.ui.screens.ProximityTestScreen
import com.hamraj37.devpulse.ui.screens.SpeakerTestScreen
import com.hamraj37.devpulse.ui.screens.VibrationTestScreen
import com.hamraj37.devpulse.ui.screens.VolumeButtonTestScreen
import com.hamraj37.devpulse.ui.theme.DevPulseTheme

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()

        val testId = intent.getStringExtra(EXTRA_TEST_ID) ?: ""

        setContent {
            DevPulseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (testId) {
                        "automatic" -> AutomaticTestsScreen(onBack = { finish() })
                        "test_touch" -> MultitouchTestScreen(
                            onBack = { finish() },
                            onPass = { setResult(RESULT_OK); finish() },
                            onFail = { setResult(RESULT_CANCELED); finish() }
                        )
                        "test_display" -> DisplayTestScreen(
                            onBack = { finish() },
                            onPass = { setResult(RESULT_OK); finish() },
                            onFail = { setResult(RESULT_CANCELED); finish() }
                        )
                        "test_flashlight" -> FlashlightTestScreen(
                            onBack = { finish() },
                            onPass = { setResult(RESULT_OK); finish() },
                            onFail = { setResult(RESULT_CANCELED); finish() }
                        )
                        "test_speaker" -> SpeakerTestScreen(
                            onBack = { finish() },
                            onPass = { setResult(RESULT_OK); finish() },
                            onFail = { setResult(RESULT_CANCELED); finish() }
                        )
                        "test_earspeaker" -> EarSpeakerTestScreen(
                            onBack = { finish() },
                            onPass = { setResult(RESULT_OK); finish() },
                            onFail = { setResult(RESULT_CANCELED); finish() }
                        )
                        "test_mic" -> MicTestScreen(
                            onBack = { finish() },
                            onPass = { setResult(RESULT_OK); finish() },
                            onFail = { setResult(RESULT_CANCELED); finish() }
                        )
                        "test_proximity" -> ProximityTestScreen(
                            onBack = { finish() },
                            onPass = { setResult(RESULT_OK); finish() },
                            onFail = { setResult(RESULT_CANCELED); finish() }
                        )
                        "test_light" -> LightSensorTestScreen(
                            onBack = { finish() },
                            onPass = { setResult(RESULT_OK); finish() },
                            onFail = { setResult(RESULT_CANCELED); finish() }
                        )
                        "test_accel" -> AccelerometerTestScreen(
                            onBack = { finish() },
                            onPass = { setResult(RESULT_OK); finish() },
                            onFail = { setResult(RESULT_CANCELED); finish() }
                        )
                        "test_charging" -> ChargingTestScreen(
                            onBack = { finish() },
                            onPass = { setResult(RESULT_OK); finish() },
                            onFail = { setResult(RESULT_CANCELED); finish() }
                        )
                        "test_vibration" -> VibrationTestScreen(
                            onBack = { finish() },
                            onPass = { setResult(RESULT_OK); finish() },
                            onFail = { setResult(RESULT_CANCELED); finish() }
                        )
                        "test_bluetooth" -> BluetoothTestScreen(
                            onBack = { finish() },
                            onPass = { setResult(RESULT_OK); finish() },
                            onFail = { setResult(RESULT_CANCELED); finish() }
                        )
                        "test_fingerprint" -> BiometricTestScreen(
                            onBack = { finish() },
                            onPass = { setResult(RESULT_OK); finish() },
                            onFail = { setResult(RESULT_CANCELED); finish() }
                        )
                        "test_gps" -> GpsTestScreen(
                            onBack = { finish() },
                            onPass = { setResult(RESULT_OK); finish() },
                            onFail = { setResult(RESULT_CANCELED); finish() }
                        )
                        "test_volume" -> VolumeButtonTestScreen(
                            onBack = { finish() },
                            onPass = { setResult(RESULT_OK); finish() },
                            onFail = { setResult(RESULT_CANCELED); finish() }
                        )
                        else -> finish()
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.show(WindowInsetsCompat.Type.statusBars())
        controller.hide(WindowInsetsCompat.Type.navigationBars())
    }

    companion object {
        const val EXTRA_TEST_ID = "extra_test_id"
    }
}
