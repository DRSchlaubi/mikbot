import com.github.gmazzo.gradle.plugins.BuildConfigField

plugins {
    com.github.gmazzo.buildconfig apply false
}

abstract class TemplateExtension {
    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val className: Property<String>
}

extensions.create<TemplateExtension>("template")

val extension = project.extensions.findByName("template") as TemplateExtension

buildConfig {
    packageName = extension.packageName
    className = extension.className

    buildConfigField("String", "VERSION", "\"${project.version}\"")
//    buildConfigField("String", "BRANCH", provider { "\"${project.getGitBranch()}\"" })
//    buildConfigField("String", "COMMIT", provider { "\"${project.getGitCommit()}\"" })
}
