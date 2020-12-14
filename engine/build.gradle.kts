plugins {
    `java-library`
}

dependencies {
    implementation(project(":core"))
    implementation(project(":buffer"))
    implementation(project(":cache"))
    implementation(project(":utility"))
    implementation(project(":network"))
}
tasks.withType<Test> {
    jvmArgs("-XX:-OmitStackTraceInFastThrow")
}