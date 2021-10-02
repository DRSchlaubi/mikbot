package dev.schlaubi.musicbot.game.module.commands

import java.text.DecimalFormat

private val ratioFormat = DecimalFormat("00%") // percentage

fun Double.formatPercentage(): String = ratioFormat.format(this)
