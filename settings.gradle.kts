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
    "game:uno",
    "game:game-api",
    "game:uno-game",
    "game:music-quiz",
    "utils",
    "utils:ktor",
    "utils:verification-system",
    "utils:epic-games-notifier",
    "test-bot",
    "mikmusic-bot",
    "votebot",
    "votebot:chart-service-client"
    "utils:profiles",
    "utils:role-selector",
    "mikmusic-bot",
)

includeBuild("gradle-plugin")
