dependencies {
    implementation(project(":game:entity:player:dialogue"))
    implementation(project(":game:api"))
    implementation(project(":game:skill:woodcutting"))
    implementation(project(":game:skill:mining"))
    testImplementation(project(":game:area:misthalin:varrock"))
    testImplementation(project(":game:area:kandarin:ardougne"))
    testImplementation(project(":game:area:kandarin:tree-gnome-stronghold"))
    testImplementation(project(":game:entity:obj:teleport"))
    testImplementation(project(":game:api:testing"))
}