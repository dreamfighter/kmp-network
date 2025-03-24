plugins {
    kotlin("multiplatform")
    id("module.publication")
}

kotlin {

    jvm()

    sourceSets {

        val jvmMain by getting {
            dependencies {
                implementation(libs.ksp.processor.api)
                implementation(libs.kotlinpoet)
                implementation(project(":request-annotation"))
            }
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }

    }
}
