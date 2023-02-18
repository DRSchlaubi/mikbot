plugins {
    id("com.gradle.enterprise") version "3.12"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

rootProject.name = "mikmusic"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
    "music",
    "clients:haste-client",
    "clients:sponsorblock-kt",
    "clients:image-color-client",
    "clients:image-color-client-kord"
)

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
