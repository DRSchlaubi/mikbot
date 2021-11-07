import java.nio.file.Files

plugins {
    id("com.google.devtools.ksp") // used for plugin-processor
    kotlin("jvm")
    kotlin("kapt") // used for pf4j processor
}

val pluginExtensionName = "mikbotPlugin"

extensions.create<PluginExtension>(pluginExtensionName)

val pluginMainPath = buildDir.toPath().resolve("plugin-reports").resolve(name)
val pluginMainFile = pluginMainPath.resolve("plugin-main-class.txt")

val plugin by configurations.creating
val optionalPlugin by configurations.creating

configurations {

    compileOnly {
        extendsFrom(plugin)
        extendsFrom(optionalPlugin)
    }
}

dependencies {
    compileOnly(project(":api"))
    kapt("org.pf4j", "pf4j", "3.6.0")
    ksp(project(":plugin-processor"))
}

ksp {
    arg("output-dir", pluginMainPath.toAbsolutePath().toString())
}

tasks {
    jar {
        dependsOn("kspKotlin")
    }

    afterEvaluate {
        jar {
            val mainClass = Files.readString(pluginMainFile)
            val extension = project.extensions.getByName<PluginExtension>(pluginExtensionName)
            manifest {
                attributes["Plugin-Class"] = mainClass
                attributes["Plugin-Id"] = project.name
                attributes["Plugin-Version"] = project.version
                extension.requires.orNull?.let { requires ->
                    attributes["Plugin-Requires"] = requires
                }
                buildDependenciesString().takeIf { it.isNotBlank() }?.let { dependencies ->
                    attributes["Plugin-Dependencies"] = dependencies
                }

                attributes["Plugin-Description"] = extension.description.getOrElse("<no description>")
                attributes["Plugin-Provider"] = extension.provider.getOrElse("MikBot Contributors")
                attributes["Plugin-License"] = extension.license.getOrElse("Apache 2.0")
            }
        }
    }
}

fun buildDependenciesString(): String {
    val required = plugin.allDependencies.map { it.toDependencyString() }
    val optional = optionalPlugin.allDependencies.map { it.toDependencyString(true) }

    return (required + optional).joinToString(", ")
}

fun Dependency.toDependencyString(optional: Boolean = false): String {
    val name = if (this is ProjectDependency) {
        dependencyProject.name
    } else {
        name
    }

    return "$name@$version${if (optional) "?" else ""}"
}

abstract class PluginExtension {
    abstract val mainClass: Property<String>
    abstract val requires: Property<String>
    abstract val dependencies: ListProperty<String>
    abstract val description: Property<String>
    abstract val provider: Property<String>
    abstract val license: Property<String>
}
