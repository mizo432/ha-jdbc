import org.gradle.api.plugins.JavaPluginExtension

val haJdbcVersion = "4.0.0-alpha-1-SNAPSHOT"

subprojects {
    group = "io.ha-jdbc"
    version = haJdbcVersion

    plugins.withId("java") {
        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_25
            targetCompatibility = JavaVersion.VERSION_25
        }

        tasks.withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }

    repositories {
        mavenCentral()
    }
}
