dependencies {
    val engine = project(":engine")
    implementation(engine)
    testImplementation(engine.dependencyProject.sourceSets["test"].output)
    implementation(project(":cache"))
    implementation(project(":network"))
    implementation(project(":utility"))
    implementation(kotlin("script-runtime"))
}