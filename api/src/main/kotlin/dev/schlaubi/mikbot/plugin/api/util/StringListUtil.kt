package dev.schlaubi.mikbot.plugin.api.util

public fun List<String>.splitIntoPages(pageLength: Int, separator: String = "\n"): List<String> {
    // Split strings into sub-pages, if one string is longer than a page
    if (any { it.length > pageLength }) return flatMap { it.chunked(pageLength) }.splitIntoPages(pageLength, separator)

    var currentLength = 0
    var currentList = ArrayList<String>(10)
    val paged = ArrayList<String>(size)

    val iterator = iterator()

    fun addCurrentList() {
        paged.add(currentList.joinToString(separator))
    }

    while (iterator.hasNext()) {
        val line = iterator.next()
        val fullLength = line.length + "\n".length
        if ((currentLength + fullLength) > pageLength) {
            addCurrentList()
            currentList = ArrayList(10)
            currentLength = 0
        }

        currentList.add(line)
        currentLength += fullLength
    }
    addCurrentList()

    return paged
}
