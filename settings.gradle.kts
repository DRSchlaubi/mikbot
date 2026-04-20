plugins {
    id("com.gradle.develocity") version "4.4.1"
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
    }
}

rootProject.name = "mikmusic"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

if (System.getenv("BUILD_PLUGIN_CI")?.toBoolean() != true) {
    include(
        "api",
        "api:annotations",
        "plugin-processor",
        "runtime",
        "core:database-i18n",
        "core:game-animator",
        "core:gdpr",
        "core:kubernetes",
        "core:redeploy-hook",
        "core:ktor",
        ":music",
        "music:player",
        "music:commands",
        "music:lyrics",
        "music:api",
        "music:api:types",
        "music:api:server",
        "clients:haste-client",
        "clients:image-color-client",
        "clients:image-color-client-kord"
    )
}

includeBuild("gradle-plugin")
