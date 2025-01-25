dependencies {
    implementation(project(":game:entity:player:dialogue"))
    implementation(project(":game:api"))
    testImplementation(project(":game:area:wilderness"))
    testImplementation(project(":game:area:misthalin:varrock"))
    testImplementation(project(":game:entity:npc:npc-combat"))
    testImplementation(project(":game:skill:prayer"))
    testImplementation(project(":game:entity:player:equipment"))
    testImplementation(project(":game:api:testing"))
}