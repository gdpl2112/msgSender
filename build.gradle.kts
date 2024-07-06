plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.16.0"
}

group = "io.github.gdpl2112"
version = "1.5"

repositories {
    maven("https://repo1.maven.org/maven2/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    mavenLocal()
    mavenCentral()
}

mirai {
    noTestCore = true
    setupConsoleTestRuntime {
        // 移除 mirai-core 依赖
        classpath = classpath.filter {
            !it.nameWithoutExtension.startsWith("mirai-core-jvm")
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))

//    compileOnly("net.mamoe:mirai-core:2.15.0")
    testConsoleRuntime("top.mrxiaom:overflow-core:2.16.0-db61867-SNAPSHOT")

    testImplementation("net.mamoe:mirai-logging-slf4j:2.15.0")

    implementation(platform("org.slf4j:slf4j-parent:2.0.6"))
    testImplementation("org.slf4j:slf4j-simple")

    implementation("io.github.Kloping:JvUtils:0.4.9-R3")

    implementation("org.xerial:sqlite-jdbc:3.41.2.2")
}

mirai {
    jvmTarget = JavaVersion.VERSION_11
}
