plugins {
    `kotlin-dsl`
}

group = "world.gregs.config"
version = 1

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = java.sourceCompatibility

repositories {
    mavenCentral()
}


dependencies {
    implementation(libs.kasechange)
    implementation(libs.fastutil)

    testImplementation(libs.mockk)
}