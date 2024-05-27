plugins {
    id("com.gradle.enterprise") version "3.17.4"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
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
        "core:health",
        "core:redeploy-hook",
        "core:ktor",
        ":music",
        "music:player",
        "music:commands",
        "music:lyrics",
        "clients:discord-oauth",
        "clients:haste-client",
        "clients:image-color-client",
        "clients:image-color-client-kord"
    )
}

includeBuild("gradle-plugin")

buildCache {
    remote<HttpBuildCache> {
        isPush = (System.getenv("GRADLE_BUILD_CACHE_PUSH") == "true") && (System.getenv("IS_PR") == "false")
        url = uri("https://gradle-build-cache.srv02.schlaubi.net/cache/")
        val cacheUsername = System.getenv("GRADLE_BUILDCACHE_USERNNAME")
        val cachePassword = System.getenv("GRADLE_BUILDCACHE_PASSWORD")
        if (cacheUsername != null && cachePassword != null) {
            credentials {
                username = cacheUsername
                password = cachePassword
            }
        }
    }
}
