import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    java
    id("org.springframework.boot") version "3.5.3" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.github.ben-manes.versions") version "0.51.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

val lombokVersion by extra("1.18.36")
val mapstructVersion by extra("1.5.5.Final")
val testcontainersVersion by extra("1.20.2")
val springCloudVersion by extra("2025.0.0")

allprojects {
    repositories {
        mavenCentral()
    }
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    group = "verification"
    description = "Checks for outdated dependencies"
    gradleReleaseChannel = "current"
    outputFormatter = "json"
    outputDir = "$rootDir/build/dependencyUpdates"
    reportfileName = "${rootProject.name}-dependency-updates"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    }

    // Импорт BOM Spring Cloud через dependency-management
    configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
        }
    }

    // ==== Общие тестовые и утилитные зависимости ====
    dependencies {
        // Lombok
        compileOnly("org.projectlombok:lombok:$lombokVersion")
        annotationProcessor("org.projectlombok:lombok:$lombokVersion")

        // MapStruct
        implementation("org.mapstruct:mapstruct:$mapstructVersion")
        annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

        // Тестовый стек
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
        testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    // ==== Kafka-модули (продюсеры/консьюмеры) ====
    val kafkaModules = setOf(
        project(":accounts-service"),
        project(":cash-service"),
        project(":transfer-service"),
        project(":exchange-service"),
        project(":exchange-generator-service"),
        project(":notifications-service"),
    )
    if (project in kafkaModules) {
        dependencies {
            implementation("org.springframework.kafka:spring-kafka")
            testImplementation("org.springframework.kafka:spring-kafka-test")
        }
    }

    // ==== Модули с JPA/БД ====
    val jpaModules = setOf(
        project(":accounts-service"),
    )
    if (project in jpaModules) {
        dependencies {
            implementation("org.springframework.boot:spring-boot-starter-data-jpa")
            runtimeOnly("org.postgresql:postgresql")
            implementation("org.springframework:spring-webflux")
        }
    }

    // ==== Ресурсные серверы (JWT) ====
    val resourceServerModules = jpaModules + setOf(
        project(":cash-service"),
        project(":transfer-service"),
        project(":exchange-service"),
        project(":blocker-service"),
    )
    if (project in resourceServerModules) {
        dependencies {
            implementation(project(":common-lib"))
            implementation("org.springframework.boot:spring-boot-starter-web")
            implementation("org.springframework.boot:spring-boot-starter-security")
            implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
            implementation("org.springframework.boot:spring-boot-starter-validation")
            testImplementation("org.springframework.security:spring-security-test")
            implementation("org.springframework.boot:spring-boot-starter-actuator")
        }
    }

    // ==== Клиентские модули (исходящие вызовы с client_credentials) ====
    val clientModules = setOf(
        project(":exchange-generator-service"),
        project(":accounts-service"),
        project(":cash-service"),
        project(":transfer-service"),
        project(":blocker-service"),
        // project(":notifications-service"), // <--- убрали
    )
    if (project in clientModules) {
        dependencies {
            implementation(project(":common-lib"))
            implementation("org.springframework.boot:spring-boot-starter-web")
            implementation("org.springframework.boot:spring-boot-starter-security")
            implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
            implementation("org.springframework:spring-webflux")
            testImplementation("org.springframework.security:spring-security-test")
        }
    }

    // ==== Notifications: чистый Kafka-воркер без Web ====
    val notificationsModules = setOf(
        project(":notifications-service"),
    )
    if (project in notificationsModules) {
        dependencies {
            // без common-lib, чтобы не тащить oauth2-resource-server
            implementation("org.springframework.boot:spring-boot-starter")
            implementation("org.springframework.boot:spring-boot-starter-actuator")
            // spring-kafka уже добавлен через kafkaModules
        }
    }

//    // === Consul Config (исторически, сейчас не используем)
//    val consulModules = setOf(
//        project(":gateway"),
//        project(":accounts-service"),
//        project(":cash-service"),
//        project(":transfer-service"),
//        project(":exchange-service"),
//        project(":exchange-generator-service"),
//        project(":notifications-service"),
//        project(":blocker-service")
//    )
//    if (project in consulModules) {
//        dependencies {
//            implementation("org.springframework.cloud:spring-cloud-starter-consul-config")
//        }
//    }
}

// === gateway ===
project(":gateway") {
    dependencies {
        implementation(project(":common-lib"))
        implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
        implementation("org.springframework.boot:spring-boot-starter-security")
        implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
        implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
        testImplementation("org.springframework.security:spring-security-test")
    }
}

// === common-lib ===
project(":common-lib") {
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter")
        implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    }
}

// === db-migrations ===
project(":db-migrations") {
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter")
        implementation("org.springframework.boot:spring-boot-starter-jdbc")
        implementation("org.liquibase:liquibase-core")
        runtimeOnly("org.postgresql:postgresql")
        implementation("org.springframework.boot:spring-boot-starter-web")
    }
}
