import org.gradle.api.Project
import java.io.ByteArrayOutputStream

fun Project.getGitCommit(): String {
    return execCommand(arrayOf("git", "rev-parse", "--short", "HEAD"))
        ?: System.getenv("GITHUB_SHA") ?: "<unknown>"
}

fun Project.getGitBranch(): String {
    return execCommand(arrayOf("git", "rev-parse", "--abbrev-ref", "HEAD")) ?: "unknown"
}

private fun Project.execCommand(command: Array<String>): String? {
    return try {
        ByteArrayOutputStream().use { out ->
            exec {
                commandLine(command.asIterable())
                standardOutput = out
            }
            out.toString().trim()
        }
    } catch (e: Throwable) {
        logger.warn("An error occurred whilst executing a command", e)
        null
    }
}
