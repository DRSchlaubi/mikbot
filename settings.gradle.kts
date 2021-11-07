import java.net.URI

rootProject.name = "mikmusic"
include("uno")

sourceControl {
    gitRepository(URI.create("https://github.com/DRSchlaubi/kord.git")) {
        producesModule("kord:core")
    }
}

include("api:annotations")
include("api")

include("core")
include("core:game-animator")
include("core:gdpr")
include("core:database-i18n")
include("core:redeploy-hook")

include("music")

include("game:uno")
include("game:game-api")
include("game:uno-game")
include("game:music-quiz")

include("utils")
include("utils:verification-system")
include("plugin-processor")
