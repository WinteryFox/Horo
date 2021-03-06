import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    kotlin("jvm") version "1.3.72"
    id("com.google.cloud.tools.jib") version "2.4.0"
    idea
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

group = "bot.horo"
version = "0.0.1"
description = "A Discord bot written with love in Kotlin using Discord4J"

repositories {
    jcenter()
    maven("https://jitpack.io")
    maven("https://repo.spring.io/milestone")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.discord4j:discord4j-core:3.1.0")
    implementation("com.discord4j.stores:stores-redis:c244bed")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.3.7")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.0-M1")
    implementation("io.r2dbc:r2dbc-postgresql:0.8.3.RELEASE")
    implementation("io.r2dbc:r2dbc-pool:0.8.3.RELEASE")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            targetCompatibility = "11"
            sourceCompatibility = "11"
        }
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_11
    sourceCompatibility = JavaVersion.VERSION_11
}

jib {
    to.image = "winteryfox/horobot:latest"
    container.mainClass = "bot.horo.MainKt"
}