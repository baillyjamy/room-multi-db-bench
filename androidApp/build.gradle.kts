plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "bench.app"
    compileSdk = 36
    defaultConfig {
        applicationId = "bench.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release {
            // Non-debuggable build for realistic ART performance; debug key so it installs directly.
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.coroutines.android)
}
