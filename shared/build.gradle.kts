import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
}

kotlin {
    android {
        namespace = "bench.shared"
        compileSdk = 36
        minSdk = 26
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    iosSimulatorArm64 {
        binaries.executable {
            entryPoint = "bench.main"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.coroutines.core)
        }
        getByName("iosSimulatorArm64Main") {
            kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/iosSimulatorArm64/iosSimulatorArm64Main/kotlin"))
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        optIn.add("kotlin.experimental.ExperimentalNativeApi")
        optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
        optIn.add("kotlin.native.runtime.NativeRuntimeApi")
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

tasks.named("compileKotlinIosSimulatorArm64") { dependsOn("kspKotlinIosSimulatorArm64") }
