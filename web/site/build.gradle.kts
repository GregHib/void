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
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.12.0")
    implementation("org.jetbrains:markdown:0.7.12")
}
