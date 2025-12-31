val exposed_version: String by project
val h2_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val postgres_version: String by project
val prometheus_version: String by project
val mockk_version: String by project
val junit_platform_launcher_version: String by project

plugins {
    kotlin("jvm") version "2.2.20"
    id("io.ktor.plugin") version "3.3.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
    id("org.sonarqube") version "7.0.1.6134"
    id("jacoco")
}

group = "com.collektar"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

sourceSets {
    main {
        kotlin.srcDirs("src/main/kotlin")
        resources.srcDirs("src/main/resources")
    }
    test {
        kotlin.srcDir("test/kotlin")
        resources.srcDir("test/resources")
    }
}

dependencies {
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("com.h2database:h2:$h2_version")
    implementation("org.postgresql:postgresql:$postgres_version")

    implementation("io.ktor:ktor-server-host-common")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-swagger")
    implementation("io.ktor:ktor-server-metrics-micrometer")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheus_version")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlin_version")
    testImplementation("io.mockk:mockk:$mockk_version")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("io.ktor:ktor-client-mock")
    testImplementation("io.ktor:ktor-client-content-negotiation")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junit_platform_launcher_version")
}

jacoco {
    toolVersion = "0.8.14"
}

tasks.test {
    useJUnitPlatform()

    environment("DB_URL", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;")
    environment("DB_USER", "sa")
    environment("DB_PASSWORD", "")

    maxParallelForks = 1
    forkEvery = 1

    systemProperty("ktor.environment", "test")

    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    sourceSets(sourceSets.main.get())
}

sonar {
    properties {
        property("sonar.projectKey", "collektar_Collection-Service")
        property("sonar.organization", "ase-group-a")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "build/reports/jacoco/test/jacocoTestReport.xml"
        )
    }
}