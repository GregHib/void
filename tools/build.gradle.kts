plugins {
    id("shared")
}

kotlin {
    dependencies {
        implementation(project(":buffer"))
        implementation(project(":cache"))
        implementation(project(":engine"))
        implementation(project(":game"))
        implementation(project(":network"))
        implementation(project(":database"))
        implementation(libs.exposed)
        implementation(libs.exposed.jdbc)
        implementation(project(":config"))
        implementation(project(":types"))

        implementation("org.seleniumhq.selenium:selenium-java:4.16.1")

        implementation(libs.kotlinx.io)
        implementation(libs.kotlinx.coroutines)

        implementation("org.apache.commons:commons-compress:1.24.0")
        implementation("org.jsoup:jsoup:1.17.2")
        implementation("org.sweble.wikitext:swc-engine:3.1.9")
        implementation("com.github.weisj:darklaf-core:2.7.3")
        implementation("javax.xml.bind:jaxb-api:2.3.1")
        implementation(libs.kasechange)

        implementation(libs.koin)
        implementation(libs.displee.cache)
        implementation("com.fasterxml.jackson.core:jackson-core:2.16.1")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
        implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.16.1")
        implementation(libs.fastutil)
        implementation(libs.rsmod.pathfinder)

        testImplementation(libs.mockk)
    }
}

tasks.withType<AbstractTestTask>().configureEach {
    failOnNoDiscoveredTests = false
}

tasks.withType<JavaExec> {
    workingDir = projectDir
}

tasks.register<JavaExec>("importPetTranscript") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("world.gregs.voidps.tools.cache.ImportPetTranscript")
    workingDir = rootDir
}

tasks.register<JavaExec>("fixEnums") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("world.gregs.voidps.tools.cache.FixEnums")
    workingDir = rootDir
}

tasks.register<JavaExec>("renderPhotoBooth") {
    group = "photobooth"
    description = "Renders photo-booth avatars. Args via -Pargs=\"--player=name --out=/tmp/avatar\" (see PhotoBoothRenderer)."
    mainClass.set("world.gregs.voidps.tools.photobooth.PhotoBoothRenderer")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = rootDir
    val cliArgs = (findProperty("args") as String?)?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()
    args = cliArgs
}
