
apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

dependencies {
    implementation(project(":buffer"))
    implementation(project(":cache"))
    implementation(project(":utility"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
}
tasks.withType<Test> {
    jvmArgs("-XX:-OmitStackTraceInFastThrow")
}