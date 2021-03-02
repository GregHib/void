dependencies {
    implementation(kotlin("compiler-embeddable"))
    implementation(project(":cache"))
    implementation("org.koin:koin-core:${findProperty("koinVersion")}")
}