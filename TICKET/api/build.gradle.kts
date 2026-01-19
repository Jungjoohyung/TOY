plugins {
    id("java")
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
}

group = "com.toy"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // 1. Core 모듈 연결
    implementation(project(":core"))

    // 2. 웹 기능
    implementation("org.springframework.boot:spring-boot-starter-web")

    // 3. JPA 기능 (★ 이 줄이 없어서 에러가 났던 겁니다!)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // 4. 롬복
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // 5. 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    
    // 6. swagger (SpringDoc)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
}

tasks.test {
    useJUnitPlatform()
}