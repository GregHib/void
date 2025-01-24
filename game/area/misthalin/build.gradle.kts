dependencies {
    implementation(project(":game:entity:player:dialogue"))
    implementation(project(":game:area:global"))
    implementation(project(":game:api"))
    implementation(project(":game:quest"))
    testImplementation(project(":game:area:misthalin:lumbridge"))
    testImplementation(project(":game:area:kharidian-desert:al-kharid"))
    testImplementation(project(":game:area:misthalin:wizards-tower"))
    testImplementation(project(":game:api:testing"))
}