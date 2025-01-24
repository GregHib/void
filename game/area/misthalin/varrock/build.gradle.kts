dependencies {
    implementation(project(":game:entity:player:dialogue"))
    implementation(project(":game:area:global"))
    implementation(project(":game:api"))
    implementation(project(":game:quest"))
    implementation(project(":game:skill:runecrafting"))
    implementation(project(":game:skill:woodcutting"))
    implementation(project(":game:skill:mining"))
    testImplementation(project(":game:api:testing"))
}