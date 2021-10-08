package dev.schlaubi.musicbot.utils

import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import dev.kord.core.behavior.UserBehavior
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlin.math.ceil

/**
 * Configures this [PaginatorBuilder] to create one page for each [x][chunkSize] elements in [items]
 * .
 *
 * @param user the [PaginatorBuilder.owner] of this paginator
 * @param items a [List] containing all the items
 * @param mapper a mapper converting [T] to [String]
 * @param title a function providing the title for the current page
 * @param enumerate whether to include element numbers in entries or not
 * @param additionalConfig additional [PaginatorBuilder] config
 * @param additionalPageConfig additional [EmbedBuilder] config, applied to each page
 */
suspend fun <T> PaginatorBuilder.forList(
    user: UserBehavior,
    items: List<T>,
    mapper: suspend (T) -> String,
    title: suspend (current: Int, total: Int) -> String,
    chunkSize: Int = 8,
    enumerate: Boolean = true,
    additionalConfig: suspend PaginatorBuilder.() -> Unit = {},
    additionalPageConfig: suspend EmbedBuilder.() -> Unit = {},
) = forList(user, items.size, { offset, limit ->
    items.subList(offset, (offset + limit).coerceAtMost(items.size))
}, mapper, title, chunkSize, enumerate, additionalConfig, additionalPageConfig)

/**
 * Configures this [PaginatorBuilder] to create one page for each [x][chunkSize] elements in [items]
 * .
 *
 * @param user the [PaginatorBuilder.owner] of this paginator
 * @param total the total amount of items
 * @param items a [Flow] containing all the items
 * @param mapper a mapper converting [T] to [String]
 * @param title a function providing the title for the current page
 * @param enumerate whether to include element numbers in entries or not
 * @param additionalConfig additional [PaginatorBuilder] config
 * @param additionalPageConfig additional [EmbedBuilder] config, applied to each page
 */
suspend fun <T> PaginatorBuilder.forFlow(
    user: UserBehavior,
    total: Long,
    items: Flow<T>,
    mapper: suspend (T) -> String,
    title: suspend (current: Int, total: Int) -> String,
    chunkSize: Int = 8,
    enumerate: Boolean = true,
    additionalConfig: suspend PaginatorBuilder.() -> Unit = {},
    additionalPageConfig: suspend EmbedBuilder.() -> Unit = {},
) = forList(user, total.toInt(), { offset, _ ->
    items.drop(offset).take(chunkSize).toList()
}, mapper, title, chunkSize, enumerate, additionalConfig, additionalPageConfig)

private suspend fun <T> PaginatorBuilder.forList(
    user: UserBehavior,
    size: Int,
    subList: suspend (offset: Int, limit: Int) -> List<T>,
    mapper: suspend (T) -> String,
    title: suspend (current: Int, end: Int) -> String,
    chunkSize: Int = 8,
    enumerate: Boolean = true,
    additionalConfig: suspend PaginatorBuilder.() -> Unit = {},
    additionalPageConfig: suspend EmbedBuilder.() -> Unit = {},
) {
    owner = user

    var currentIndexOffset = 0

    repeat(ceil(size / chunkSize.toDouble()).toInt()) {
        val items = subList(currentIndexOffset, chunkSize)
        addPage(currentIndexOffset, title, items, enumerate, mapper, additionalPageConfig)

        currentIndexOffset += items.size
    }

    additionalConfig()
}

private fun <T> PaginatorBuilder.addPage(
    myOffset: Int,
    title: suspend (current: Int, end: Int) -> String,
    items: List<T>,
    enumerate: Boolean,
    mapper: suspend (T) -> String,
    additionalPageConfig: suspend EmbedBuilder.() -> Unit
) {
    page {
        this.title = title((myOffset + 1), pages.size)

        description =
            items.mapIndexed { index, it ->
                if (enumerate) {
                    "${index + myOffset + 1}: ${mapper(it)}"
                } else {
                    mapper(it)
                }
            }
                .joinToString("\n")

        additionalPageConfig()
    }
}
