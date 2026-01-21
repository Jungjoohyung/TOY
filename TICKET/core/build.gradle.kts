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
    // 1. JPA (자바 객체랑 DB 테이블 연결)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // 2. MySQL 드라이버
    runtimeOnly("com.mysql:mysql-connector-j")

    // 3. 롬복
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // 4. 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly ("org.junit.platform:junit-platform-launcher")
    
    //5. JWT (로그인 토큰용)
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

}


// ▼▼▼ 여기가 문제 해결의 열쇠입니다 ▼▼▼

// 1. "야, 너는 실행 파일 만들지 마!" (에러 원인 차단)
tasks.bootJar {
    enabled = false
}

// 2. "대신 다른 애들이 갖다 쓸 수 있게 포장만 해!"
tasks.jar {
    enabled = true
    // ★ 이거 중요: 'core-plain.jar'가 아니라 'core.jar'로 이름표를 예쁘게 붙여줌
    archiveClassifier.set("") 
}

tasks.withType<Test> {
    testLogging {
        // "System.out.println" 내용을 콘솔에 보여줘라!
        showStandardStreams = true

        showStandardStreams = true // 숨겨진 로그(println)를 보여줘라!
        events("passed", "skipped", "failed")
    }
}