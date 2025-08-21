plugins {
    id("com.google.devtools.ksp") version "1.9.22-1.0.18"
}

dependencies {
    implementation(project(":buffer"))
    implementation(project(":cache"))
    implementation(project(":engine"))
    implementation(project(":game"))
    implementation(project(":network"))
    implementation(project(":config"))
    implementation(project(":types"))

    implementation("org.seleniumhq.selenium:selenium-java:4.16.1")

    implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:${findProperty("kotlinIoVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${findProperty("kotlinCoroutinesVersion")}")

    implementation("org.apache.commons:commons-compress:1.24.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.sweble.wikitext:swc-engine:3.1.9")
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
