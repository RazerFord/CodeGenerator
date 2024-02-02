plugins {
    id("java")
}

group = "org.src"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val jacodbVersion = "1.4.4"

    implementation("org.apache.commons:commons-lang3:3.14.0")

    implementation(group = "org.jacodb", name = "jacodb-api", version = jacodbVersion)
    implementation(group = "org.jacodb", name = "jacodb-core", version = jacodbVersion)
    implementation(group = "org.jacodb", name = "jacodb-analysis", version = jacodbVersion)

    implementation("io.github.kostaskougios:cloning:1.10.3")

    implementation("org.jetbrains:annotations:24.0.0")
    implementation("com.squareup:javapoet:1.13.0")
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