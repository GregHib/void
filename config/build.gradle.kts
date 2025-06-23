dependencies {
    implementation("net.pearx.kasechange:kasechange:${findProperty("kaseChangeVersion")}")
    implementation("it.unimi.dsi:fastutil:${findProperty("fastUtilVersion")}")

    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
}
