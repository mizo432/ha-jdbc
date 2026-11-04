plugins {
    `java-library`
}

dependencies {
    // HA-JDBC modules (test scope)
    testImplementation(project(":core"))
    testImplementation(project(":distributed:jgroups"))
    testImplementation(project(":i18n:gettext"))
    testImplementation(project(":state:sqlite"))

    // Test dependencies
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.vintage)
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
