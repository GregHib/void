dependencies {
    implementation(project(":ai"))
    implementation(project(":buffer"))
    implementation(project(":cache"))
    implementation(project(":engine"))
    implementation(project(":utility"))

    implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:${findProperty("kotlinIoVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${findProperty("kotlinCoroutinesVersion")}")

    implementation("org.jsoup:jsoup:1.13.1")
    implementation("org.sweble.wikitext:swc-engine:2.0.0")
    implementation("com.github.weisj:darklaf-core:2.5.3")
    implementation("org.koin:koin-core:${findProperty("koinVersion")}")
    implementation("com.displee:rs-cache-library:${findProperty("displeeCacheVersion")}")
    implementation("io.netty:netty-all:${findProperty("nettyVersion")}")
    implementation("com.fasterxml.jackson.core:jackson-core:${findProperty("jacksonVersion")}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${findProperty("jacksonVersion")}")

    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
}