import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
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
        // Static framework linked by the SwiftUI app in iosApp/.
        binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    macosArm64 {
        binaries.executable {
            entryPoint = "bench.main"
        }
    }

    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        binaries {
            executable {
                mainClass.set("bench.MainKt")
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.coroutines.core)
        }
        // KSP output of native targets is not registered automatically in this setup.
        listOf("iosSimulatorArm64", "macosArm64").forEach { target ->
            getByName("${target}Main") {
                kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/$target/${target}Main/kotlin"))
            }
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
    add("kspMacosArm64", libs.room.compiler)
    add("kspJvm", libs.room.compiler)
}

tasks.named("compileKotlinIosSimulatorArm64") { dependsOn("kspKotlinIosSimulatorArm64") }
tasks.named("compileKotlinMacosArm64") { dependsOn("kspKotlinMacosArm64") }
