plugins {
    `mikbot-plugin`
    `mikbot-module`
}

group = "dev.schlaubi.mikbot"
version = "1.2.0"

dependencies {
    implementation("org.mozilla", "rhino", "1.7.14")
    implementation(projects.utils.hasteClient)
    ksp("com.kotlindiscord.kord.extensions", "annotation-processor", "1.5.2-RC1")
}

mikbotPlugin {
    description.set("Plugin allowing users to execute code.")
}

kotlin {
    sourceSets {
        main {
            kotlin.srcDir("build/generated/ksp/main/kotlin")
        }
        test {
            kotlin.srcDir("build/generated/ksp/test/kotlin")
        }
    }
}
