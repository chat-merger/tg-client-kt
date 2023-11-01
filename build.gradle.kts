import com.google.protobuf.gradle.id

plugins {
    id("com.google.protobuf") version "0.9.2"
    kotlin("jvm") version "1.9.0"

    application
}

repositories {
    mavenCentral() // не удалять, котлин упадет
    gradlePluginPortal()
}

object Version {
    const val grpc = "1.59.0"
    const val javax = "1.3.2"
    const val protobuf = "3.24.4"
    const val grpcKotlin = "1.4.0"
    const val coroutines = "1.7.3"
    const val protobufPlugin = "0.9.2"
}

val libs = listOf (
//    "io.grpc:grpc-protobuf-lite:${Version.grpc}",
//    "com.google.protobuf:protoc:${Version.protobuf}",
//    "com.google.protobuf:protobuf-java:${Version.protobuf}",
//    "javax.annotation:javax.annotation-api:${Version.javax}",
//    "com.google.protobuf:protobuf-javalite:${Version.protobuf}",
//    "com.google.protobuf:protobuf-gradle-plugin:${Version.protobufPlugin}",


    "io.grpc:grpc-netty:${Version.grpc}",
    "io.grpc:protoc-gen-grpc-java:${Version.grpc}",
    "io.grpc:grpc-kotlin-stub:${Version.grpcKotlin}",
    "com.google.protobuf:protobuf-kotlin:${Version.protobuf}",
    "io.grpc:protoc-gen-grpc-kotlin:${Version.grpcKotlin}:jdk8@jar",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.coroutines}",
)
//    "io.grpc:grpc-stub:${Version.grpc}",

dependencies {
    libs.forEach {
        implementation(it)
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${Version.protobuf}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${Version.grpc}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${Version.grpcKotlin}:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

application {
    // функция fun main неявно создает вокруг себя класс по имени файлы, сбольшой буквы + Kt  в конце
    mainClass.set("MainKt")
}
