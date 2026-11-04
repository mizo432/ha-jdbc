import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import org.gradle.api.plugins.JavaPluginExtension

val haJdbcVersion = "4.0.0-alpha-1-SNAPSHOT"

plugins {
    id("com.github.spotbugs") version "6.5.12" apply false
}

subprojects {
    group = "io.ha-jdbc"
    version = haJdbcVersion

    plugins.withId("java") {
        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
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
            // SpotBugsが警告を検出してもビルドが成功するようにする
            setIgnoreFailures(true)
        }
    }

    repositories {
        mavenCentral()
    }
}
