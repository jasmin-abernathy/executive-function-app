plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Release signing is deliberately opt-in. No keystore or password is stored in Git.
val betaStoreFile = providers.environmentVariable("TDAH_BETA_STORE_FILE")
val betaStorePassword = providers.environmentVariable("TDAH_BETA_STORE_PASSWORD")
val betaKeyAlias = providers.environmentVariable("TDAH_BETA_KEY_ALIAS")
val betaKeyPassword = providers.environmentVariable("TDAH_BETA_KEY_PASSWORD")
val hasBetaSigning = listOf(
    betaStoreFile, betaStorePassword, betaKeyAlias, betaKeyPassword,
).all { it.isPresent }


android {
    namespace = "org.lepotager.executivefunction"
    compileSdk = 37

    defaultConfig {
        applicationId = "org.lepotager.executivefunction"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0-dev"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = false
    }

    signingConfigs {
        if (hasBetaSigning) {
            create("betaRelease") {
                storeFile = file(betaStoreFile.get())
                storePassword = betaStorePassword.get()
                keyAlias = betaKeyAlias.get()
                keyPassword = betaKeyPassword.get()
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasBetaSigning) {
                signingConfig = signingConfigs.getByName("betaRelease")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")

    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
