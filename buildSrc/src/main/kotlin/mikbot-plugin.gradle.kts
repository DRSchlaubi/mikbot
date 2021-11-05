plugins {
    kotlin("jvm")
    kotlin("kapt")
}

dependencies {
    compileOnly(project(":api"))
    kapt("org.pf4j", "pf4j", "3.6.0")
}
