plugins {
    `mikbot-plugin`
    `mikbot-module`
}

group = "dev.schlaubi.mikbot"
version = "2.2.0"

dependencies {
    implementation(libs.rhino)
    implementation(projects.utils.hasteClient)
    ksp(libs.kordex.processor)
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
