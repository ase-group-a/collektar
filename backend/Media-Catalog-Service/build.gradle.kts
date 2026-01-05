val koin_version: String by project
val kotlin_version: String by project
val logback_version: String by project
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

dependencies {
    //BGG JSON PARSE
    implementation("org.jsoup:jsoup:1.17.2")
    ///
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-metrics-micrometer")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheus_version")
    implementation("io.ktor:ktor-server-swagger")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-server-host-common")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-client-logging")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlin_version")

    testImplementation("io.mockk:mockk:$mockk_version")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("io.ktor:ktor-client-mock")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junit_platform_launcher_version")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude("**/integration/tmdb/TmdbClient.*",
                        "**/di/AppModules.*"
                    )
                }
            }
        )
    )
}


jacoco {
    toolVersion = "0.8.14"
}


sonar {
    properties {
        property("sonar.projectKey", "collektar_Media-Catalog-Service")
        property("sonar.organization", "ase-group-a")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
        property(
            "sonar.coverage.exclusions",
            "src/main/kotlin/integration/tmdb/TmdbClient.kt, src/main/kotlin/di/AppModules.kt"
        )
    }
}
