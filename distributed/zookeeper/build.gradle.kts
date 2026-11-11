plugins {
    `java-library`
    jacoco
}

dependencies {
    api(project(":core"))
    api("org.apache.curator:curator-x-discovery:5.7.1")
    api("org.apache.curator:curator-framework:5.7.1")
    api("org.apache.curator:curator-client:5.7.1")
    api("org.apache.curator:curator-recipes:5.7.1")

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.mockito.core)
    testImplementation(libs.assertj.core)
}
