import java.io.File
import java.util.Properties
import java.util.Base64
import java.security.KeyStore

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
}

fun isValidKeyStore(file: File, storePass: String, alias: String): Boolean {
    if (!file.exists() || file.length() == 0L) return false
    return try {
        val ks = KeyStore.getInstance("JKS")
        file.inputStream().use { stream ->
            ks.load(stream, storePass.toCharArray())
        }
        ks.containsAlias(alias)
    } catch (_: Exception) {
        try {
            val ks = KeyStore.getInstance("PKCS12")
            file.inputStream().use { stream ->
                ks.load(stream, storePass.toCharArray())
            }
            ks.containsAlias(alias)
        } catch (_: Exception) {
            false
        }
    }
}

android {
    namespace = "com.hamraj37.devpulse"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.hamraj37.devpulse"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val localPropsFile = rootProject.file("local.properties")
            val localProps = Properties()
            var rawKeystoreBase64: String? = null

            if (localPropsFile.exists()) {
                try {
                    localPropsFile.useLines { lines ->
                        for (line in lines) {
                            val trimmed = line.trim()
                            if (trimmed.startsWith("KEYSTORE_BASE64=")) {
                                rawKeystoreBase64 = trimmed.substringAfter("KEYSTORE_BASE64=")
                                break
                            }
                        }
                    }
                    localPropsFile.inputStream().use { localProps.load(it) }
                } catch (_: Exception) {}
            }

            val keystoreBase64 = System.getenv("KEYSTORE_BASE64")
                ?: rawKeystoreBase64
                ?: localProps.getProperty("KEYSTORE_BASE64")
                ?: project.findProperty("KEYSTORE_BASE64") as? String

            val storePass = System.getenv("KEYSTORE_PASSWORD")
                ?: localProps.getProperty("KEYSTORE_PASSWORD")
                ?: localProps.getProperty("KEYSTORE_PASSWORD:")
                ?: "Hamraj37Key"
            val aliasName = System.getenv("KEY_ALIAS")
                ?: localProps.getProperty("KEY_ALIAS")
                ?: localProps.getProperty("KEY_ALIAS:")
                ?: "Hamraj37"
            val keyPass = System.getenv("KEY_PASSWORD")
                ?: localProps.getProperty("KEY_PASSWORD")
                ?: localProps.getProperty("KEY_PASSWORD:")
                ?: "Hamraj37Key"

            val decodedFile = file("${layout.buildDirectory.get()}/decoded_keystore.jks")
            val hamrajFile = file("hamraj37.jks")

            if (!keystoreBase64.isNullOrEmpty()) {
                val cleanBase64 = keystoreBase64.trim().removePrefix("-").replace("\n", "").replace("\r", "").replace(" ", "")
                val padLength = (4 - (cleanBase64.length % 4)) % 4
                val paddedBase64 = cleanBase64 + "=".repeat(padLength)
                val decodedBytes = try {
                    Base64.getDecoder().decode(paddedBase64)
                } catch (_: Exception) {
                    ByteArray(0)
                }
                decodedFile.parentFile.mkdirs()
                if (decodedBytes.isNotEmpty()) {
                    decodedFile.writeBytes(decodedBytes)
                }
            }

            val chosenKeystore = when {
                isValidKeyStore(decodedFile, storePass, aliasName) -> decodedFile
                isValidKeyStore(hamrajFile, storePass, aliasName) -> hamrajFile
                else -> null
            }

            if (chosenKeystore != null) {
                storeFile = chosenKeystore
                storePassword = storePass
                keyAlias = aliasName
                keyPassword = keyPass
            } else {
                initWith(getByName("debug"))
            }
        }
    }

    buildTypes {
        debug {
            val relConfig = signingConfigs.getByName("release")
            if (relConfig.storeFile != null && relConfig.storeFile!!.exists()) {
                signingConfig = relConfig
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
        }
        release {
            val relConfig = signingConfigs.getByName("release")
            if (relConfig.storeFile != null && relConfig.storeFile!!.exists()) {
                signingConfig = relConfig
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.adaptive.layout)
    implementation(libs.androidx.compose.adaptive.navigation3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.coil.compose)
    implementation(libs.converter.moshi)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.logging.interceptor)
    implementation(libs.material)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    "ksp"(libs.androidx.room.compiler)
    "ksp"(libs.moshi.kotlin.codegen)
}