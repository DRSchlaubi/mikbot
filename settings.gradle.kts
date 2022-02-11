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
    "utils:ktor",
    "utils:verification-system",
    "utils:epic-games-notifier",
    "utils:profiles",
    "utils:role-selector",
    "utils:botblock",
    "votebot",
    "votebot:common",
    "votebot:chart-service-client",
    "votebot:plugin",
    "test-bot",
    "mikmusic-bot",
)

includeBuild("gradle-plugin")

if (System.getenv("GRADLE_BUILDCACHE_URL") != null) {
    buildCache {
        remote<HttpBuildCache> {
            isPush = (System.getenv("GRADLE_BUILD_CACHE_PUSH") == "true") && (System.getenv("IS_PR") == "false")
            url = uri(System.getenv("GRADLE_BUILDCACHE_URL"))
            credentials {
                username = System.getenv("GRADLE_BUILDCACHE_USERNNAME")
                password = System.getenv("GRADLE_BUILDCACHE_PASSWORD")
            }
        }
    }
}
