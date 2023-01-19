dependencies {
    implementation(project(":buffer"))
    implementation(project(":cache"))
    implementation(project(":engine"))
    implementation(project(":network"))

    implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:${findProperty("kotlinIoVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${findProperty("kotlinCoroutinesVersion")}")

    implementation("org.jsoup:jsoup:1.13.1")
    implementation("org.sweble.wikitext:swc-engine:2.0.0")
    implementation("com.github.weisj:darklaf-core:2.7.3")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("net.pearx.kasechange:kasechange:${findProperty("kaseChangeVersion")}")

    implementation("io.insert-koin:koin-core:${findProperty("koinVersion")}")
    implementation("com.displee:rs-cache-library:${findProperty("displeeCacheVersion")}")
    implementation("com.fasterxml.jackson.core:jackson-core:${findProperty("jacksonVersion")}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${findProperty("jacksonVersion")}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${findProperty("jacksonVersion")}")
    implementation("it.unimi.dsi:fastutil:${findProperty("fastUtilVersion")}")
    implementation("org.rsmod:rsmod-pathfinder:${findProperty("pathfinderVersion")}")

    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
}