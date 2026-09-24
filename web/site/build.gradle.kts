plugins {
    id("shared")
    application
    alias(libs.plugins.kotlinSerialization)
}

application {
    mainClass.set("world.gregs.voidps.web.site.Site")
    tasks.run.get().workingDir = rootProject.projectDir
}

dependencies {
    implementation(project(":buffer"))
    implementation(project(":cache"))
    implementation(project(":types"))
    implementation(project(":engine"))
    implementation(libs.kotlinx.html)
    implementation(libs.markdown)
    implementation(libs.kotlinx.serialization.json)
}
