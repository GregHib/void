plugins {
    id("shared")
}

kotlin {
    dependencies {
        implementation(project(":buffer"))
        implementation(project(":cache"))
        implementation(project(":engine"))
        implementation(project(":network"))
        implementation(project(":database"))
        implementation(project(":tools:render"))
        runtimeOnly(project(":game"))
        implementation(libs.displee.cache)
    }
}

tasks.register<JavaExec>("renderPhotoBooth") {
    group = "photobooth"
    description = "Renders photo-booth avatars. Args via -Pargs=\"--player=name --out=/tmp/avatar\" (see PhotoBoothRenderer)."
    mainClass.set("world.gregs.voidps.tools.avatar.PhotoBoothRenderer")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = rootDir
    val cliArgs = (findProperty("args") as String?)?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()
    args = cliArgs
}
