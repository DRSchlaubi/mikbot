package dev.schlaubi.mikbot.utils.roleselector.util

fun <E> Iterable<E>.replace(old: E, new: E) = map { if (it == old) new else it }
