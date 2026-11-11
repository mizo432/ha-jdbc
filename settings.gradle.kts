rootProject.name = "ha-jdbc"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include("core")
include("distributed")
include("distributed:jgroups")
include("i18n")
include("i18n:gettext")
include("logging")
include("logging:commons")
include("logging:slf4j")
include("state")
include("state:leveldb")
include("state:sqlite")
include("test")
include("sample-springboot4")
