plugins {
    `mikbot-module`
    `mikbot-plugin`
    `mikbot-template`
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}

dependencies {
    api(projects.votebot.common)
    implementation(projects.votebot.chartServiceClient)
    implementation(libs.java.string.similarity)
    ksp(libs.kordex.processor)
    optionalPlugin(projects.core.gdpr)
}

mikbotPlugin {
    description.set("Plugin adding VoteBot functionality")
    bundle.set("votebot")
    pluginId.set("votebot")
}

template {
    packageName.set("space.votebot")
    className.set("VoteBotInfo")
}

sourceSets {
    main {
        java {
            srcDir(file("$buildDir/generated/ksp/main/kotlin/"))
        }
    }
}

tasks {
    val bundledPlugins = listOf(":core:gdpr", ":core:database-i18n", ":utils:botblock")

    val assembleVoteBot = task<Zip>("assembleVoteBot") {
        destinationDirectory.set(project.buildDir.resolve("bot"))
        archiveBaseName.set("votebot-${project.name}")
        archiveExtension.set("zip")

        into("") {
            from(project(":").tasks["installDist"])
        }
        val installedPluginsName = "lib/bundled-plugins"
        into(installedPluginsName) {
            val provider = assemblePlugin.flatMap { task -> task.archiveFile }
            from(provider)
        }
        into(installedPluginsName) {
            bundledPlugins.forEach { plugin ->
                from(project(plugin).tasks["assemblePlugin"])
            }
        }
    }

    register<Copy>("installVoteBotArchive") {
        dependsOn(assembleVoteBot)

        from(zipTree(assembleVoteBot.archiveFile))
        into(buildDir.resolve("installVoteBot"))
    }
}

fun version(name: String) = project(name).version
