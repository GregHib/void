plugins {
    id("shared")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.12.0")
    implementation("org.jetbrains:markdown:0.7.12")
}

tasks.register<JavaExec>("exchangePreview") {
    group = "application"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("world.gregs.voidps.web.site.ExchangePreviewMain")
}
