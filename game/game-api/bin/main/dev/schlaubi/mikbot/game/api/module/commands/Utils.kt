package dev.schlaubi.mikbot.game.api.module.commands

import java.text.DecimalFormat

private val ratioFormat = DecimalFormat("00%") // percentage

fun Double.formatPercentage(): String = ratioFormat.format(this)
