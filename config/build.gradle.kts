dependencies {
    implementation("net.pearx.kasechange:kasechange:${findProperty("kaseChangeVersion")}")
    implementation("it.unimi.dsi:fastutil:${findProperty("fastUtilVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.6.3")
    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
}
