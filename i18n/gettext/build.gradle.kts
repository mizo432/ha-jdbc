plugins {
    `java-library`
}

dependencies {
    api(project(":core"))
    api(libs.gettext.commons)
}
