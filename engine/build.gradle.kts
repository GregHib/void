plugins {
    `java-library`
}

dependencies {
    compile(project(":cache"))
    compile(project(":network"))
    compile(project(":tools"))
}