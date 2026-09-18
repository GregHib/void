plugins {
    id("shared")
    application
}

application {
    mainClass.set("world.gregs.voidps.web.site.Site")
    tasks.run.get().workingDir = rootProject.projectDir
}

dependencies {
    implementation(project(":cache"))
    implementation(project(":engine"))
    implementation(libs.kotlinx.html)
    implementation(libs.markdown)
}
