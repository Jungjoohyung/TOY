dependencies {
    // 프로젝트 의존성
    implementation(project(":common"))
    
    // Spring Boot
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.validation)
    
    // Database
    runtimeOnly(libs.mysql.connector)
    
    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    
    // Testing
    testImplementation(libs.spring.boot.starter.test)
}