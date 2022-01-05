package dev.schlaubi.mikbot.plugin.api.util

public fun String.ensurePath(): String =
    if (isWindows()) {
        if (startsWith("file://")) {
            drop("file://".length)
        } else {
            drop(1)
        }
    } else {
        this
    }

private fun isWindows() = System.getProperty("os.name").startsWith("Windows")
