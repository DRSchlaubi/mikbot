#!/usr/bin/env kotlin
@file:DependsOn(
    "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.10",
    "com.vdurmont:semver4j:3.1.0"
)

import com.vdurmont.semver4j.Semver
import java.nio.file.Files
import kotlin.io.path.*
import kotlin.streams.asSequence

val blacklist = listOf("test-bot", "mikmusic-bot", "gradle-plugin", "plugin-processor", "image-color-client", "google-emotes")

val rootDir = Path("").absolute()

val regexes = listOf(
    Regex("version = \\\"([\\w\\.]+)\\\""),
    Regex("\"([\\w.]+)\"\\s+//\\sversion marker")
)

Files.walk(rootDir)
    .asSequence()
    .filter { it.isDirectory() }
    .filterNot { blacklist.any { blacklist -> blacklist in it.relativeTo(rootDir).toString() } }
    .forEach { path ->
        path.listDirectoryEntries("{build.gradle.kts,Project.kt}").forEach { file ->
            val content = file.readText()
            regexes.mapNotNull { regex ->
                regex.find(content)
            }.forEach { result ->
                val (version) = result.destructured
                val semver = Semver(version).nextMinor()
                println(
                    "Bumped %s from %s to %s".format(
                        file.relativeTo(rootDir).toString(),
                        version,
                        semver.toString()
                    )
                )
                val newContent = content.replace(result.value, result.value.replace(version, semver.toString()))
                file.writeText(newContent)
            }
        }
    }
