dependencies {
    implementation(project(":buffer"))
    implementation(project(":cache"))
    implementation(project(":utility"))
}
tasks.withType<Test> {
    jvmArgs("-XX:-OmitStackTraceInFastThrow")
}