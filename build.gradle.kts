plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val jacodbVersion = "1.4.4"

    implementation(group = "org.jacodb", name = "jacodb-api", version = jacodbVersion)
    implementation(group = "org.jacodb", name = "jacodb-core", version = jacodbVersion)
    implementation(group = "org.jacodb", name = "jacodb-analysis", version = jacodbVersion)

    implementation("org.jetbrains:annotations:24.0.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.test {
    useJUnitPlatform()
}