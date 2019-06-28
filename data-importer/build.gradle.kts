import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

val group = "scholars-discovery-import"
val version = "1.0.0"

object Versions {
    const val kotlinVersion: String = "1.3.31"
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.31")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.31"
    java
    application
    id("org.jlleitschuh.gradle.ktlint") version "7.4.0"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClassName = "duke.MainKt"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-progressive")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

ktlint {
    verbose.set(false)
    outputToConsole.set(true)
    coloredOutput.set(true)
    ignoreFailures.set(true)
    enableExperimentalRules.set(false)
    reporters.set(setOf(ReporterType.CHECKSTYLE))
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    // implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.31")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.31")
    implementation(group = "org.apache.jena", name = "apache-jena-libs", version = "3.10.0")
    implementation(group = "org.apache.logging.log4j", name = "log4j-slf4j-impl", version = "2.10.0")
    implementation("org.apache.logging.log4j:log4j:2.8.2")
    implementation(group = "org.apache.logging.log4j", name = "log4j-api", version = "2.10.0")
    implementation(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.10.0")

    // implementation(group = "org.seaborne.rdf-delta", name = "rdf-delta-base", version = "0.7.0")
    // https://mvnrepository.com/artifact/org.seaborne.rdf-delta/rdf-patch
    // compile group: 'org.seaborne.rdf-delta', name: 'rdf-patch', version: '0.7.0' 

    // https://mvnrepository.com/artifact/org.seaborne.rdf-delta/rdf-delta
    // compile group: 'org.seaborne.rdf-delta', name: 'rdf-delta', version: '0.7.0', ext: 'pom'

    // https://mvnrepository.com/artifact/org.seaborne.rdf-delta/rdf-delta-dist
    compile(group = "org.seaborne.rdf-delta", name = "rdf-delta-dist", version = "0.7.0", ext = "pom")
}
