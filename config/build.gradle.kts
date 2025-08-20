plugins {
    kotlin("plugin.serialization") version "1.9.22"
    id("com.google.devtools.ksp") version "1.9.22-1.0.18"
}

dependencies {
    implementation("net.pearx.kasechange:kasechange:${findProperty("kaseChangeVersion")}")
    implementation("it.unimi.dsi:fastutil:${findProperty("fastUtilVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.6.3")
    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.22-1.0.18")
    implementation("com.squareup:kotlinpoet:1.18.1")
    implementation("com.squareup:kotlinpoet-ksp:1.18.1")
}
