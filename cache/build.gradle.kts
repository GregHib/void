import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

dependencies {
    implementation(project(":buffer"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0-RC")
    implementation("com.displee:rs-cache-library:${findProperty("displeeCacheVersion")}")
    implementation("io.netty:netty-all:${findProperty("nettyVersion")}")
    implementation("org.koin:koin-core:${findProperty("koinVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")

    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger-jvm:${findProperty("inlineLoggingVersion")}")

    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes", "-Xopt-in=kotlin.RequiresOptIn")
}
tasks.withType<Test> {
    jvmArgs("-XX:-OmitStackTraceInFastThrow")
}