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
    // 공통 모듈이라 별다른 의존성 없어도 됩니다. 롬복 정도만 넣죠.
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}

// [핵심] Common 모듈도 실행 파일(bootJar)이 아니라 라이브러리(jar)입니다.
tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
    archiveClassifier.set("")
}