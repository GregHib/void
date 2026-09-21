plugins {
    id("shared")
    application
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
}
