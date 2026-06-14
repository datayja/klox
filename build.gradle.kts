plugins {
    kotlin("jvm") version "2.2.21"
}

group = "datayja.klox"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.arrow.bom))
    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(24)
}

afterEvaluate {
    project.tasks.register("runMain", JavaExec::class) {
        classpath = project.sourceSets["main"].runtimeClasspath
        mainClass = "datayja.klox.KloxKt"

        standardInput = System.`in`
        standardOutput = System.`out`
    }
}
