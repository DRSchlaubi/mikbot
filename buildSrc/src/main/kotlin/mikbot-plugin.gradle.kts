import dev.schlaubi.mikbot.gradle.MakeRepositoryIndexTask

plugins {
    id("com.google.devtools.ksp") // used for plugin-processor
    kotlin("jvm")
    id("dev.schlaubi.mikbot.gradle-plugin")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8")) // this one is included in the bot itself
    compileOnly(project(":api"))
    ksp(project(":plugin-processor"))
}

tasks {
    "buildRepository"(MakeRepositoryIndexTask::class) {
        targetDirectory.set(rootProject.file("ci-repo").toPath())
    }
}
