repositories {
    maven(url = "https://dl.bintray.com/michaelbull/maven")
}

dependencies {
    implementation(project(":buffer"))
    implementation("com.displee:rs-cache-library:${findProperty("displeeCacheVersion")}")
    implementation("io.netty:netty-all:${findProperty("nettyVersion")}")
    implementation("org.koin:koin-core:${findProperty("koinVersion")}")

    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger-jvm:${findProperty("inlineLoggingVersion")}")

    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
}