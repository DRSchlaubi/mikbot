import java.nio.file.Files

plugins {
    application
}

group = "dev.schlaubi"
version = "2.0.1"

application {
    mainClass.set("dev.schlaubi.mikbot.tester.LauncherKt")
}

dependencies {
    if(name != "test-bot") {
        implementation(project(":test-bot"))
    }
}

tasks {
    val pluginsTxt = file("plugins.txt").toPath()
    val plugins = if(Files.exists(pluginsTxt)) {
        Files.readAllLines(pluginsTxt)
            .asSequence()
            .filterNot { it.startsWith("#") }
            .filterNot { it.isBlank() }
            .toList()
    } else {
        emptyList()
    }
    val pluginsDirectory = file("plugins")

    val deleteObsoletePlugins = task<Delete>("deleteObsoletePlugins") {
        file("plugins").listFiles()?.forEach {
            delete(it)
        }
    }

    val installPlugins = task<Copy>("installPlugins") {
        dependsOn(deleteObsoletePlugins)

        outputs.dir(pluginsDirectory)

        plugins.forEach {
            dependsOn("$it:assemblePlugin")

            from(project(it).buildDir.resolve("plugin"))
            include("*.zip")
            into(pluginsDirectory)
        }
    }

    val exportProjectPath = task("exportProjectPath") {
        val output = buildDir.resolve("resources").resolve("main").resolve("bot-project-path.txt")
        outputs.file(output)

        doFirst {
            val path = output.toPath()
            Files.writeString(path, projectDir.absolutePath)
        }
    }

    classes {
        dependsOn(exportProjectPath, installPlugins)
    }
}
