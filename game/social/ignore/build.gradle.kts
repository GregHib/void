dependencies {
    implementation(project(":game:entity:player:dialogue"))
    implementation(project(":game:api"))

    testImplementation(project(":game:social:chat"))
    testImplementation(project(":game:api:testing"))
}