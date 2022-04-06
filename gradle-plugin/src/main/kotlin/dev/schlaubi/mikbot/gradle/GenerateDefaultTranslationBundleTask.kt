package dev.schlaubi.mikbot.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import java.util.*

abstract class GenerateDefaultTranslationBundleTask : DefaultTask() {
    @get:Input
    abstract val defaultLocale: Property<Locale>

    @get:Input
    abstract val bundles: ListProperty<String>


    @TaskAction
    fun copyBundle() {
        val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer
        val from = sourceSets.getByName("main").resources
        val to = project.buildDir.resolve("generated").resolve("mikbot").resolve("main").resolve("resources")

        val result = project.copy { spec ->
            with(spec) {
                val locale = defaultLocale.orNull?.resourceBundleKey
                    ?: error("Please specify a default locale")
                from(from)
                into(to)

                bundles.get().ifEmpty { listOf("strings") }.forEach {
                    val name = "${it}_${locale}.properties"
                    include("translations/${project.bundle}/$name")
                    rename(name, "${it}.properties")
                }
            }
        }
        didWork = result.didWork
    }
}

val Locale.resourceBundleKey get() = toLanguageTag().replace('-', '_')
