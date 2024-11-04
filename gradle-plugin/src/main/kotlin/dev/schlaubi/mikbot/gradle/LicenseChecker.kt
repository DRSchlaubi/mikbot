package dev.schlaubi.mikbot.gradle

import com.github.jk1.license.LicenseReportExtension
import com.github.jk1.license.LicenseReportPlugin
import com.github.jk1.license.render.JsonReportRenderer
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.*

fun Project.configureLicenseChecker() {
    apply<LicenseReportPlugin>()
    val output = layout.buildDirectory.dir("generated/mikbot")

    configure<LicenseReportExtension> {
        outputDir = output.get().asFile.absolutePath
        renderers = arrayOf(JsonReportRenderer("license-report.json", true))
        configurations = arrayOf("runtimeClasspath")
    }

    the<SourceSetContainer>().named("main") {
        resources.srcDir(output)
    }

    with(tasks) {
        named("processResources") {
            dependsOn("generateLicenseReport")
        }
    }
}
