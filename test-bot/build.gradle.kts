import java.nio.file.Files

plugins {
    `mikbot-module`
    application
}

group = "dev.schlaubi"
version = "2.0.1"

application {
    mainClass.set("dev.schlaubi.mikbot.tester.LauncherKt")
}

dependencies {
    implementation(project(":"))
}

tasks {
    val pluginsTxt = file("plugins.txt")
    val plugins = Files.readAllLines(pluginsTxt.toPath())
        .asSequence()
        .filterNot { it.startsWith("#") }
        .filterNot { it.isBlank() }
        .toList()
    val pluginsDirectory = file("plugins")

    val deleteObsoletePlugins = task<Delete>("deleteObsoletePlugins") {
        file("plugins").listFiles()!!.forEach {
            delete(it)
        }
    }

    val installPlugins = task<Copy>("installPlugins") {
        dependsOn(deleteObsoletePlugins)

        inputs.file(pluginsTxt)
        outputs.dir(pluginsDirectory)

        plugins.forEach {
            dependsOn("$it:assemblePlugin")

            from(project(it).buildDir.resolve("plugin"))
            include("*.zip")
            into(pluginsDirectory)
        }
    }

    classes {
        dependsOn(installPlugins)
    }
}
