package dev.schlaubi.musicbot.utils

fun List<String>.splitIntoPages(pageLength: Int, separator: String = "\n"): List<String> {
    val paged = ArrayList<String>(size)
    var currentLength = 0
    var currentList = ArrayList<String>(10)

    for (line in this) {
        val fullLength = line.length + "\n".length
        if ((currentLength + fullLength) > pageLength) {
            paged.add(currentList.joinToString(separator))
            currentList = ArrayList(10)
            currentLength = 0
        }

        currentList.add(line)
        currentList + fullLength
    }

    return paged
}
