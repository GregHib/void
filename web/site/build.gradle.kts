plugins {
    id("shared")
}

dependencies {
    implementation(project(":cache"))
    implementation(project(":engine"))
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.12.0")
    implementation("org.jetbrains:markdown:0.7.12")
}
