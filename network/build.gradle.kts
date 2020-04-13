dependencies {
    val engine = project(":engine")
    implementation(engine)
    testImplementation(engine.dependencyProject.sourceSets["test"].output)
    implementation(project(":cache"))
    implementation(project(":world"))
    implementation(project(":utility"))
}