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
    "utils:profiles",
    "utils:role-selector",
    "votebot",
    "votebot:common",
    "votebot:chart-service-client",
    "votebot:plugin",
    "test-bot",
    "mikmusic-bot",
)

includeBuild("gradle-plugin")
