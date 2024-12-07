import org.gradle.api.Project
import java.io.ByteArrayOutputStream

fun Project.getGitCommit(): String {
    return execCommand("git", "rev-parse", "--short", "HEAD")
        ?: System.getenv("GITHUB_SHA") ?: "<unknown>"
}

fun Project.getGitBranch(): String = execCommand("git", "rev-parse", "--abbrev-ref", "HEAD") ?: "unknown"

internal fun Project.execCommand(vararg command: String): String? {
    val output = ByteArrayOutputStream()
    try {
        providers.exec {
            commandLine("git", *command)
            standardOutput = output
            errorOutput = output
            workingDir = rootDir
        }.result.get().rethrowFailure()
    } catch (e: Exception) {
        return null
    }
    return output.toString().trim()
}
