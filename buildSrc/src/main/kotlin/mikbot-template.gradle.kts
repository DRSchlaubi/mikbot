plugins {
    id("com.github.gmazzo.buildconfig")
}

abstract class TemplateExtension {
    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val className: Property<String>
}

extensions.create<TemplateExtension>("template")

afterEvaluate {
    val extension = project.extensions.findByName("template") as TemplateExtension

    buildConfig {
        packageName(extension.packageName.get())
        className(extension.className.get())
        buildConfigField("String", "VERSION", "\"${project.version}\"")
        buildConfigField("String", "BRANCH", "\"${project.getGitBranch()}\"")
        buildConfigField("String", "COMMIT", "\"${project.getGitCommit()}\"")
    }
}
