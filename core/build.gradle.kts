plugins {
    `java-library`
    jacoco
}

// Resource filtering for .properties files with @token@ syntax
tasks.withType<ProcessResources> {
    filesMatching("**/*.properties") {
        filter { line ->
            line.replace("@project.version@", project.version.toString())
                 .replace("@project.groupId@", project.group.toString())
        }
    }
}

// Include .properties from source directory (Maven behavior)
tasks.named<ProcessResources>("processResources") {
    val sourceDir = layout.projectDirectory.dir("src/main/java")
    if (sourceDir.asFile.exists()) {
        from(sourceDir) {
            include("**/*.properties")
            filter { line ->
                line.replace("@project.version@", project.version.toString())
                     .replace("@project.groupId@", project.group.toString())
            }
        }
    }
}

dependencies {
    // SpotBugs annotations for @SuppressFBWarnings
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.10.2")

    // Optional state manager dependencies
    api(libs.commons.pool2)

    // Test dependencies
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.vintage)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.mockito.core)
    testImplementation(libs.assertj.core)
    testImplementation(libs.hsqldb)
    testImplementation(libs.h2)
}
