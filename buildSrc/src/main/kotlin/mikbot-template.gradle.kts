import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    java
    kotlin("jvm")
}

abstract class TemplateExtension {
    @get:Input
    abstract val files: ListProperty<String>

    @get:Input
    abstract val tokens: MapProperty<String, Any?>
}

extensions.create<TemplateExtension>("template")

afterEvaluate {
    val extension = project.extensions.findByName("template") as TemplateExtension
    val files = extension.files.get().map { "**/$it" }

    tasks {
        val sourcesForRelease = task<Copy>("sourcesForRelease") {
            from("src/main/java") {
                include(files)

                val tokens = extension.tokens.get()
                filter<ReplaceTokens>(mapOf("tokens" to tokens))
            }
            into("build/filteredSrc")
            includeEmptyDirs = false
        }

        compileJava {
            dependsOn(sourcesForRelease)

            source(sourcesForRelease.destinationDir)
        }

        compileKotlin {
            dependsOn(sourcesForRelease)

            source(sourcesForRelease.destinationDir)
        }
    }

    sourceSets {
        main {
            java {
                // provided by sourcesForRelease task
                exclude(files)
            }
        }
    }
}
