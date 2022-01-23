package dev.schlaubi.mikbot.game.google_emotes

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.ReactionEmoji

/**
 * Emote for the Google Logo letter `G`.
 */
val capitalG = ReactionEmoji.Custom(name = "google_g_capital", id = Snowflake(933015489393860608), isAnimated = false)

/**
 * Emote for the Google Logo letter `o` (red).
 */
val redO = ReactionEmoji.Custom(name = "google_o_red", id = Snowflake(933015489280606268), isAnimated = false)

/**
 * Emote for the Google Logo letter `o` (yellow).
 */
val yellowO = ReactionEmoji.Custom(name = "google_o_yellow", id = Snowflake(933015489272221706), isAnimated = false)

/**
 * Emote for the Google Logo letter `g`.
 */
val smallG = ReactionEmoji.Custom(name = "google_g", id = Snowflake(933015489326772304), isAnimated = false)

/**
 * Emote for the Google Logo letter `l`.
 */
val l = ReactionEmoji.Custom(name = "google_l", id = Snowflake(933015488433369088), isAnimated = false)

/**
 * Emote for the Google Logo letter `e`.
 */
val e = ReactionEmoji.Custom(name = "google_e", id = Snowflake(933015489356115989), isAnimated = false)

/**
 * A white Google G.
 */
val googleLogoWhite = ReactionEmoji.Custom(name = "google", id = Snowflake(934638916684877875), isAnimated = false)

/**
 * A colored Google G logo.
 */
val googleLogoColor = ReactionEmoji.Custom(name = "google_color", id = Snowflake(934639130942509086), isAnimated = false)

/**
 * All letters of the Google logo.
 */
val google = listOf(capitalG, redO, yellowO, smallG, l, e)
