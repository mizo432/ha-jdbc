plugins {
    `java-library`
}

dependencies {
    api(project(":core"))
    api(libs.leveldb.api)
}
