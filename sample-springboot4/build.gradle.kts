plugins {
    java
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "11.1.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.spring.io/milestone")
    }
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Hibernate Envers for audit trail
    implementation("org.springframework.data:spring-data-envers")

    // PostgreSQL Driver
    runtimeOnly("org.postgresql:postgresql")

    // HikariCP Connection Pool
    runtimeOnly("com.zaxxer:HikariCP")

    // HA-JDBC for database redundancy
    implementation(project(":core"))
    implementation(project(":logging:slf4j"))
    implementation(project(":state:sqlite"))
    
    // H2 for HA-JDBC state management
    runtimeOnly("com.h2database:h2")

    // Flyway for database migration
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-resttestclient")
    testImplementation("org.springframework.boot:spring-boot-restclient")
    testRuntimeOnly("com.h2database:h2")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

flyway {
    url = System.getenv("DB_MASTER_URL") ?: "jdbc:postgresql://localhost:5432/sample_db"
    user = System.getenv("DB_USERNAME") ?: "sample_user"
    password = System.getenv("DB_PASSWORD") ?: "sample_password"
}
