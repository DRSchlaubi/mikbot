import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "2.10.0"

dependencies {
    plugin(projects.game.gameApi)
    plugin(projects.music)
    plugin(projects.game.multipleChoiceGame)
    optionalPlugin(projects.core.gdpr)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-Xcontext-receivers")
        }
    }
}

mikbotPlugin {
    description.set("Plugin providing Song Quizzes")
    bundle.set("song_quiz")
}
