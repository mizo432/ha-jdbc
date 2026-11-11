import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

val haJdbcVersion = "4.0.0-alpha-1-SNAPSHOT"

plugins {
    id("com.github.spotbugs") version "6.5.13" apply false
    jacoco
}

repositories {
    mavenCentral()
}

subprojects {
    group = "io.ha-jdbc"
    version = haJdbcVersion

    plugins.withId("java") {
        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_25
            targetCompatibility = JavaVersion.VERSION_25
        }

        dependencies {
            // JSpecify annotations for nullability
            add("compileOnly", "org.jspecify:jspecify:1.0.0")
        }

        tasks.withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        tasks.withType<Test> {
            useJUnitPlatform()
        }

        apply(plugin = "com.github.spotbugs")

        configure<com.github.spotbugs.snom.SpotBugsExtension> {
            effort.set(Effort.MAX)
            reportLevel.set(com.github.spotbugs.snom.Confidence.DEFAULT)
        }

        tasks.withType<SpotBugsTask>().configureEach {
            reports {
                maybeCreate("html").getRequired().set(true)
                maybeCreate("xml").getRequired().set(true)
            }
            excludeFilter.set(rootProject.layout.projectDirectory.file("spotbugs-exclude.xml"))
        }
    }

    repositories {
        mavenCentral()
    }
}

// Aggregated JaCoCo test coverage report for all subprojects
val jacocoCoverageData = files()
subprojects.forEach { subproject ->
    subproject.plugins.withId("jacoco") {
        subproject.tasks.withType<org.gradle.testing.jacoco.tasks.JacocoReport>().configureEach {
            jacocoCoverageData.from(provider { this@configureEach.executionData })
        }
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    description = "Generates an aggregate JaCoCo test coverage report for all subprojects."
    group = "verification"

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    executionData(jacocoCoverageData)

    subprojects.forEach { subproject ->
        subproject.plugins.withId("jacoco") {
            sourceSets(subproject.extensions.getByType<org.gradle.api.tasks.SourceSetContainer>()
                .named("main").get())
        }
    }

    subprojects.forEach { subproject ->
        subproject.plugins.withId("jacoco") {
            dependsOn(subproject.tasks.named("test"))
        }
    }
}

