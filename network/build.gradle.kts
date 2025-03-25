import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.serializationPlugin)
    id("module.publication")
    //id("io.github.ttypic.swiftklib") version "0.6.3"
    alias(libs.plugins.ksp)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "kmp-network"
        browser{
            testTask {
                useKarma{
                    useChrome()
                }
            }
        }
        binaries.executable()
    }
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    /*
    cocoapods {
        ios.deploymentTarget = "11.0"
        framework {
            baseName = "MultiplatformApi"
            isStatic = true
        }
        //pod("GoogleSignIn")
    }

     */

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            //implementation(libs.github.mirzemehdi.google)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.startup.runtime)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.kotlinx.coroutines.android)
            implementation(compose.components.resources)
            implementation(libs.ktor.client.okhttp)
            //implementation(libs.github.mirzemehdi.google)
            //implementation("com.google.devtools.ksp:symbol-processing-api:2.0.21-1.0.25")
        }
        val commonMain by getting {
            //kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                //put your multiplatform dependencies here
                implementation(compose.runtime)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.slf4j.simple)
                implementation(kotlin("reflect"))
                implementation(project(":request-annotation"))

                implementation(libs.ktor.server.websockets)
                implementation(libs.ktor.server.cors)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.encoding)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.test)

                implementation(libs.slf4j.simple)
                implementation(libs.kotlin.test)
                implementation(libs.system.lambda)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(project(":ksp"))
            }
        }
    }
}


dependencies {
    add("kspCommonMainMetadata", project(":ksp"))
    //add("kspAndroid", project(":ksp"))
    //add("kspAndroidTest", project(":ksp"))
    //add("kspIosX64", project(":ksp"))
    //add("kspIosArm64", project(":ksp"))
    //add("kspIosSimulatorArm64", project(":ksp"))
}
//

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

kotlin.sourceSets.commonMain {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}

ksp {
    arg("measureDuration", "true")
}

/* */

android {
    namespace = "id.dreamfighter.kmp.network"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
