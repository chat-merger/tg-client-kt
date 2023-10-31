plugins {
    kotlin("jvm") version "1.9.0"
    application
}

repositories {
    mavenCentral() // не удалять, котлин упадет
}

application {
    // функция fun main неявно создает вокруг себя класс по имени файлы, сбольшой буквы + Kt  в конце
    mainClass.set("MainKt")
}
