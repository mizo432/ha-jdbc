plugins {
    `java-library`
    jacoco
}

dependencies {
    api(project(":core"))
    api(libs.pekko.actor)
    api(libs.pekko.actor.typed)
    api(libs.pekko.cluster.typed)
    api(libs.pekko.remote)
    api(libs.pekko.cluster.tools)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.mockito.core)
    testImplementation(libs.assertj.core)
}
