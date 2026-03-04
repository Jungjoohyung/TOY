import org.flywaydb.gradle.task.FlywayMigrateTask

buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath("com.mysql:mysql-connector-j:8.3.0")
        classpath("org.flywaydb:flyway-mysql:10.15.0")
    }
}

plugins {
    id("java")
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    id("org.flywaydb.flyway") version "10.15.0"
}

group = "com.toy"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // 1. Core 모듈 연결
    implementation(project(":core"))

    // 2. Common 모듈 연결 (ApiResponse, 공통 예외)
    implementation(project(":common"))

    // 2. 웹 기능
    implementation("org.springframework.boot:spring-boot-starter-web")

    // 3. JPA 기능 (★ 이 줄이 없어서 에러가 났던 겁니다!)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // 4. 롬복
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // 5. 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.rest-assured:rest-assured:5.4.0")
    testRuntimeOnly("com.h2database:h2")
    // 6. swagger (SpringDoc)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // 7. 입력값 검등용
    implementation ("org.springframework.boot:spring-boot-starter-validation")

    // 8. redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.redisson:redisson-spring-boot-starter:3.25.2")

    // 9. Flyway (DB 스키마 버전 관리)
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")     // MySQL 8+ 지원
    runtimeOnly("com.mysql:mysql-connector-j")      // Flyway 실행 시 직접 필요

    // 10. ShedLock (분산 스케줄러 중복 실행 방지)
    implementation("net.javacrumbs.shedlock:shedlock-spring:5.14.0")
    implementation("net.javacrumbs.shedlock:shedlock-provider-redis-spring:5.14.0")
}

tasks.test {
    useJUnitPlatform()
}

// Flyway Gradle 태스크 설정 (flywayMigrate, flywayInfo 등 직접 실행용)
flyway {
    url = "jdbc:mysql://localhost:3306/ticket_service?serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
    user = "root"
    password = "root"
    locations = arrayOf("filesystem:src/main/resources/db/migration")
}
