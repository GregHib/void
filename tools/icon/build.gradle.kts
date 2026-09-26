plugins {
    id("shared")
}

kotlin {
    dependencies {
        implementation(project(":buffer"))
        implementation(project(":cache"))
        implementation(project(":tools:render"))
        implementation(libs.displee.cache)
    }
}

tasks.register<JavaExec>("dumpItemSprites") {
    group = "icon"
    description = "Dumps every item's 36x32 inventory sprite as <id>.png and a 72x64 version as <id>_hd.png. Args via -Pargs=\"<cache dir> <output dir>\" (see CacheItemSpriteDumper)."
    mainClass.set("world.gregs.voidps.tools.icon.CacheItemSpriteDumper")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = rootDir
    jvmArgs("-Djava.awt.headless=true")
    val cliArgs = (findProperty("args") as String?)?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()
    args = cliArgs
}
