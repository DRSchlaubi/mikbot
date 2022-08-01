plugins {
    id("com.gradle.enterprise") version "3.10.2"
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
    "core",
    "core:database-i18n",
    "core:game-animator",
    "core:gdpr",
    "core:health",
    "core:redeploy-hook",
    "music",
    "game:google-emotes",
    "game:game-api",
    "game:multiple-choice-game",
    "game:uno",
    "game:uno-game",
    "game:music-quiz",
    "game:googologo",
    "game:trivia",
    "game:tic-tac-toe",
    "game:googolplex",
    "game:connect-four",
    "utils",
    "utils:birthdays",
    "utils:ktor",
    "utils:verification-system",
    "utils:epic-games-notifier",
    "utils:profiles",
    "utils:role-selector",
    "utils:botblock",
    "utils:eval",
    "utils:haste-client",
    "utils:sponsorblock-kt",
    "utils:leaderboard",
    "utils:image-color-client",
    "utils:image-color-client-kord",
    "votebot",
    "votebot:common",
    "votebot:chart-service-client",
    "votebot:plugin",
    "test-bot",
//    "mikmusic-bot",
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
