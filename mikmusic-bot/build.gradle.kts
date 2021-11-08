plugins {
    `mikbot-bot`
}

group = "dev.schlaubi"
version = "1.0.0"

dependencies {
    plugins(project(":music"))

    plugins(project(":game:game-api"))
    plugins(project(":game:uno-game"))
    plugins(project(":game:music-quiz"))
}

application {
    mainClass.set("dev.schlaubi.mikmusic.main.LauncherKt")
}

val pluginsFolder = rootDir.toPath().resolve("plugins")

tasks {
    val preparePluginsFolder = task<InstallPluginsTask>("preparePluginsFolder") {
        pluginsDirectory.set(pluginsFolder)
        configurations.plugins.get().dependencies.forEach {
            val project = (it as ProjectDependency).dependencyProject

            dependsOn("${project.path}:assemblePlugin")
        }
    }

    val copyPlugins = task<Copy>("copyPlugins") {
        dependsOn(preparePluginsFolder)
        val sources = configurations.plugins.get().dependencies.map {
            val project = (it as ProjectDependency).dependencyProject
            project.buildDir.resolve("plugin").absolutePath
        }.distinct()

        from(sources)
        include("*.zip")
        into(pluginsFolder)
    }

    classes {
        dependsOn(copyPlugins)
    }
}
