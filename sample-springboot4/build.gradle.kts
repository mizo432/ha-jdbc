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

    // Microsoft SQL Server Driver
    runtimeOnly("com.microsoft.sqlserver:mssql-jdbc")

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
    implementation("org.flywaydb:flyway-sqlserver")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-resttestclient")
    testImplementation("org.springframework.boot:spring-boot-restclient")
    testRuntimeOnly("com.h2database:h2")

    // Testcontainers for integration tests with PostgreSQL
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testRuntimeOnly("org.postgresql:postgresql")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

flyway {
    url = System.getenv("DB_MASTER_URL") ?: "jdbc:postgresql://localhost:5432/sample_db"
    user = System.getenv("DB_USERNAME") ?: "sample_user"
    password = System.getenv("DB_PASSWORD") ?: "sample_password"
}

// Docker Compose 설정 - bootRun 전에 PostgreSQL 컨테이너를 자동으로 시작
val dockerExecutable = System.getenv("DOCKER_EXECUTABLE") ?: "/usr/local/bin/docker"

tasks.register<Exec>("dockerComposeUp") {
    description = "Docker Compose 컨테이너를 시작합니다"
    group = "application"
    workingDir = projectDir
    commandLine(dockerExecutable, "compose", "up", "-d", "postgres-master", "postgres-slave")
}

tasks.register<Exec>("dockerComposeDown") {
    description = "Docker Compose 컨테이너를 중지합니다"
    group = "application"
    workingDir = projectDir
    commandLine(dockerExecutable, "compose", "down")
}

tasks.named("bootRun") {
    dependsOn("dockerComposeUp")
}
