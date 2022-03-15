package dev.schlaubi.mikbot.eval

import dev.schlaubi.envconf.getEnv

object EvalConfig {
    val HASTE_SERVER by getEnv(default = "https://pasta.with-rice.by.devs-from.asia/")
}
