package dev.schlaubi.mikbot.plugin.api.util

private val driveLetterRegex = "[A-Z]:".toRegex()

public fun String.ensurePath(): String =
    if (isWindows()) {
        val driveLetter = driveLetterRegex.find(this)
        if (driveLetter != null) {
            drop(driveLetter.range.first)
        } else {
            this
        }
    } else {
        this
    }

private fun isWindows() = System.getProperty("os.name").startsWith("Windows")
