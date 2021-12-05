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
    "mikmusic-bot",
    "utils:profiles",
    "test-bot"
)

includeBuild("gradle-plugin")
