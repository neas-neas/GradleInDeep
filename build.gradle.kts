plugins {
    kotlin("jvm") version "1.8.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.codehaus.groovy:groovy-all:3.0.13")

    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.8.10")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:1.8.10")
    implementation("org.jetbrains.kotlin:kotlin-daemon-embeddable:1.8.10")
    implementation("org.jetbrains.kotlin:kotlin-scripting-common:1.8.10")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:1.8.10")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:1.8.10")
    implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:1.8.10")
    implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-impl-embeddable:1.8.10")
    implementation("org.jetbrains.kotlin:kotlin-sam-with-receiver-compiler-plugin:1.8.10")
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.5.0")
}
