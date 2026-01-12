plugins {
    id("org.springframework.boot") apply false
}

dependencies {
    implementation(project(":common"))
    
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    
    // MySQL
    runtimeOnly("com.mysql:mysql-connector-j")
    
    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
