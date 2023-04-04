plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.14.0"
}

group = "io.github.gdpl2112"
version = "1.4"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}


dependencies {
    implementation("io.github.Kloping:JvUtils:0.4.7")
}

mirai {
    jvmTarget = JavaVersion.VERSION_11
}
