plugins {
    `java-library`
}

dependencies {
    api(project(":core"))
    api(libs.jgroups)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.vintage)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.mockito.core)
    testImplementation(libs.assertj.core)
}
