plugins {
    `java-library`
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
