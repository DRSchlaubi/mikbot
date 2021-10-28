import java.net.URI

rootProject.name = "mikmusic"
include("uno")

sourceControl {
    gitRepository(URI.create("https://github.com/DRSchlaubi/kord.git")) {
        producesModule("kord:core")
    }
}
