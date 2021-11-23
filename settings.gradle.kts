import java.net.URI

rootProject.name = "mikmusic"

sourceControl {
    gitRepository(URI.create("https://github.com/DRSchlaubi/kord.git")) {
        producesModule("kord:core")
    }
}

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
    // "mikmusic-bot",
    "game:uno",
    "game:game-api",
    "game:uno-game",
    "game:music-quiz",
    "utils",
    "utils:ktor",
    "utils:verification-system",
    "utils:epic-games-notifier"
)
include("test-bot")
