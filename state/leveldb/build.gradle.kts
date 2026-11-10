plugins {
    `java-library`
    jacoco
}

dependencies {
    api(project(":core"))
    api(libs.leveldb.api)
}
