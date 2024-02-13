plugins {
    id("java")
}

group = "org.src"
version = "0.2"

repositories {
    mavenCentral()
}

dependencies {
    val commonsLang3Version = "3.14.0"
    val jacodbVersion = "1.4.4"
    val cloningVersion = "1.10.3"
    val jbAnnotationsVersion = "24.0.0"
    val javapoetVersion = "1.13.0"
    val junitVersion = "5.9.1"

    implementation("org.apache.commons:commons-lang3:$commonsLang3Version")

    implementation("org.jacodb:jacodb-api:$jacodbVersion")
    implementation("org.jacodb:jacodb-core:$jacodbVersion")
    implementation("org.jacodb:jacodb-analysis:$jacodbVersion")

    implementation("io.github.kostaskougios:cloning:$cloningVersion")

    implementation("org.jetbrains:annotations:$jbAnnotationsVersion")
    implementation("com.squareup:javapoet:$javapoetVersion")
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
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