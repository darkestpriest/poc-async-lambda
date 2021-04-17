import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.4.32"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(platform("software.amazon.awssdk:bom:2.16.43"))
    implementation(platform("io.projectreactor:reactor-bom:Dysprosium-SR19"))

    //serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")

    //aws
    implementation("software.amazon.awssdk:dynamodb")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.0")
    implementation("com.amazonaws:aws-lambda-java-events:3.1.0")

    //reactor
    implementation("io.projectreactor.netty:reactor-netty")

    //logging
    implementation("org.apache.logging.log4j:log4j-api:2.13.3")
    implementation("org.apache.logging.log4j:log4j-core:2.13.3")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:2.13.3")
    runtimeOnly("com.amazonaws:aws-lambda-java-log4j2:1.2.0")

    testImplementation(kotlin("test-junit"))
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.13.0")
    testImplementation("org.testcontainers:localstack:1.15.2")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.hamcrest:hamcrest:2.2")

    testRuntimeOnly("com.amazonaws:aws-java-sdk-dynamodb:1.11.689")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}