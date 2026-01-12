dependencies {
    // Spring Boot
    implementation(libs.spring.boot.starter.validation)
    
    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    
    // Testing
    testImplementation(libs.spring.boot.starter.test)
}