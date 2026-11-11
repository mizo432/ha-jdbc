import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
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
    group = "com.github.mizo432"
    version = haJdbcVersion

    plugins.withId("java") {
        plugins.apply("maven-publish")

        configure<PublishingExtension> {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/mizo432/ha-jdbc")
                    credentials {
                        username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                        password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
                    }
                }
            }
            publications {
                register<MavenPublication>("gpr") {
                    from(components["java"])
                    pom {
                        name.set(project.name)
                        description.set("HA-JDBC - High Availability JDBC Driver")
                        url.set("https://github.com/mizo432/ha-jdbc")
                        licenses {
                            license {
                                name.set("MIT License")
                                url.set("https://opensource.org/licenses/MIT")
                            }
                        }
                        developers {
                            developer {
                                id.set("mizo432")
                                name.set("Kenichi Mizoguchi")
                            }
                        }
                        scm {
                            connection.set("scm:git:git://github.com/mizo432/ha-jdbc.git")
                            developerConnection.set("scm:git:ssh://git@github.com/mizo432/ha-jdbc.git")
                            url.set("https://github.com/mizo432/ha-jdbc")
                        }
                    }
                }
            }
        }

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
