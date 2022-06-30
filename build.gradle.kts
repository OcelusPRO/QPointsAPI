import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Ktor dependencies version
 */
val ktorVersion: String = "2.0.2"
/**
 * Exposed database version
 */
val exposedVersion: String = "0.38.2"

plugins {
    kotlin("jvm") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.jetbrains.dokka") version "1.7.0"
}


group = "pro.ftnl.qpointsApi"
/**
 * Compilation version
 */
val compileVersion: String = "1.1.0"

/**
 * Name of main class
 */
var mainClassName: String = "${group}.Main"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://maven.pkg.jetbrains.space/public/p/ktor/eap")
}

dependencies {
    implementation("io.github.reactivecircus.cache4k:cache4k:0.6.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.0")

    /*
     * Logger dependencies
     */
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("org.slf4j", "slf4j-api", "1.7.2")

    /*
     * Exposed dependencies
     */
    implementation("org.jetbrains.exposed:exposed-jodatime:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("mysql:mysql-connector-java:8.0.29")


    /*
     * Ktor dependencies
     */
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-serialization-gson-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-mustache-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-http-redirect-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-hsts-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-compression-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-caching-headers-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auto-head-response-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-sessions-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-network-tls-certificates-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-forwarded-header:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-resources:$ktorVersion")

    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.7.0")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs = listOf(
        "-Xjvm-default=all",  // use default methods in interfaces
        "-Xlambdas=indy"      // use invoke-dynamic lambdas instead of synthetic classes
    )
}


tasks.withType<ShadowJar> {
    archiveBaseName.set("QPointsManager")
    archiveClassifier.set("")
    archiveVersion.set(compileVersion)
}


tasks.withType<org.gradle.jvm.tasks.Jar> {
    manifest {
        attributes["Implementation-Title"] = "QPointsManager"
        attributes["Implementation-Version"] = compileVersion
        attributes["Main-Class"] = mainClassName
    }
}

tasks.dokkaHtml.configure {
    outputDirectory.set(buildDir.resolve("dokkaHtml"))
}

tasks.dokkaJavadoc.configure {
    outputDirectory.set(buildDir.resolve("dokkaJavadoc"))
}
