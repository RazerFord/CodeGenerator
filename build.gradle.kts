import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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

data class DurationOfTest(val name: String, val duration: Duration, val result: TestResult)

val top = mutableListOf<DurationOfTest>()

tasks.test {
    useJUnitPlatform()

    val events = arrayOf(
        TestLogEvent.FAILED,
        TestLogEvent.PASSED,
        TestLogEvent.SKIPPED,
        TestLogEvent.STARTED,
    )

    testLogging {
        events(*events)

        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showStackTraces = true
        showCauses = true

        debug {
            events(*events, TestLogEvent.STANDARD_ERROR, TestLogEvent.STANDARD_OUT)
            exceptionFormat = TestExceptionFormat.FULL
        }
        info.events = debug.events
        info.exceptionFormat = debug.exceptionFormat

        addTestListener(object : TestListener {
            override fun beforeSuite(suite: TestDescriptor) {}
            override fun afterSuite(suite: TestDescriptor, result: TestResult) {}
            override fun beforeTest(testDescriptor: TestDescriptor) {}
            override fun afterTest(desc: TestDescriptor, res: TestResult) {
                val duration = (res.endTime - res.startTime)
                    .toDuration(DurationUnit.MILLISECONDS)

                top.add(DurationOfTest(desc.name, duration, res))

                project.logger.lifecycle("Duration: $duration")
            }
        })

        doLast {
            project.logger.lifecycle("============================================================\n")

            project.logger.lifecycle("Three longest tests: \n")
            top.sortedWith { l, r -> r.duration.compareTo(l.duration) }
                .take(3)
                .forEach { project.logger.lifecycle("${it.name} : ${it.duration}") }

            val count = top.size
            val failed = top.count { it.result.resultType == TestResult.ResultType.FAILURE }
            val skipped = top.count { it.result.resultType == TestResult.ResultType.SKIPPED }
            val success = top.count { it.result.resultType == TestResult.ResultType.SUCCESS }

            val message = "\nTests: $count   Success: $success   Skipped: $skipped   Failed: $failed"

            project.logger.lifecycle(message)
        }
    }
}