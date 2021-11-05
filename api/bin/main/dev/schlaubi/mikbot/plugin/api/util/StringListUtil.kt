package dev.schlaubi.mikbot.plugin.api.util

public fun List<String>.splitIntoPages(pageLength: Int, separator: String = "\n"): List<String> {
    var currentLength = 0
    var currentList = ArrayList<String>(10)
    val paged = ArrayList<String>(size)

    val iterator = iterator()

    while (iterator.hasNext()) {
        val line = iterator.next()
        val fullLength = line.length + "\n".length
        if ((currentLength + fullLength) > pageLength || !iterator.hasNext() /* Add last line */) {
            paged.add(currentList.joinToString(separator))
            currentList = ArrayList(10)
            currentLength = 0
        }

        currentList.add(line)
        currentLength += fullLength
    }

    return paged
}
