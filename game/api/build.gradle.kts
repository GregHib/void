dependencies {
    implementation(project(":game:entity:player:dialogue"))
    implementation(project(":game:quest"))

    testImplementation(project(":game:api:testing"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:${findProperty("junitVersion")}")
    testImplementation("io.insert-koin:koin-test:${findProperty("koinVersion")}")
    testImplementation("io.mockk:mockk:${findProperty("mockkVersion")}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${findProperty("kotlinCoroutinesVersion")}")
}