dependencies {
    implementation(project(":ai"))
    implementation(project(":buffer"))
    implementation(project(":cache"))
    implementation(project(":engine"))
    implementation(project(":utility"))
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.0")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("org.sweble.wikitext:swc-engine:2.0.0")
    implementation("com.github.weisj:darklaf-core:2.5.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
}