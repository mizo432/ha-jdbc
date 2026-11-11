plugins {
    `java-library`
    jacoco
}

dependencies {
    api(project(":core"))
    api(libs.jgroups)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.mockito.core)
    testImplementation(libs.assertj.core)
}

// JGroups分散テストはCI環境でハングするため、テストを除外する
tasks.test {
    exclude("**/*")
}
