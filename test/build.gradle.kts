plugins {
    `java-library`
    jacoco
}

dependencies {
    // HA-JDBC modules (test scope)
    testImplementation(project(":core"))
    testImplementation(project(":distributed:pekko"))
    testImplementation(project(":i18n:gettext"))
    testImplementation(project(":state:sqlite"))

    // Test dependencies
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.mockito.core)
    testImplementation(libs.assertj.core)
    testImplementation(libs.hsqldb)
    testImplementation(libs.h2)
    testImplementation(libs.postgresql)

    // Logging
    testImplementation(libs.logback.classic)
    testImplementation(libs.logback.core)
}
