import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

object Project {
    // Mikbot version (not core plugins)
    const val version = "3.17.0"
}

val Project.mikbotVersion: String
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs").findVersion("api")
        .get().requiredVersion + "-SNAPSHOT"
