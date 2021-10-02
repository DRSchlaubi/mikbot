package dev.schlaubi.musicbot.utils

import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import dev.kord.core.behavior.UserBehavior
import dev.kord.rest.builder.message.EmbedBuilder

/**
 * Configures this [PaginatorBuilder] to create one page for each [x][chunkSize] elements in [items].
 *
 * @param user the [PaginatorBuilder.owner] of this paginator
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
) {
    owner = user

    val pages = items.mapIndexed { index, it -> it to index }.chunked(chunkSize)
    pages.forEachIndexed { index, tracks ->
        page {
            this.title = title((index + 1), pages.size)

            val enumeratingMapper: (Pair<T, Int>) -> String by lazy {
                { (it, index) -> "${index + 1}: ${mapper(it)}" }
            }
            val nonEnumeratingMapper: (Pair<T, Int>) -> String by lazy {
                { (it) -> mapper(it) }
            }

            description = tracks.joinToString(
                "\n",
                transform = if (enumerate) enumeratingMapper else nonEnumeratingMapper
            )

            additionalPageConfig()
        }
    }

    additionalConfig()
}
