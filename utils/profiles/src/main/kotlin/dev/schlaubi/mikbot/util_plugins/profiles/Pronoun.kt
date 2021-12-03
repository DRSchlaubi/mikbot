package dev.schlaubi.mikbot.util_plugins.profiles

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import kotlinx.serialization.Serializable

@Serializable
enum class Pronoun(val displayName: String, val firstPerson: String, val thirdPerson: String) : ChoiceEnum {

    SHE_HER("she/her", "she", "her"),
    HE_HIM("he/him", "he", "his"),
    THEY_THEM("they/them", "they", "their");

    override val readableName: String = displayName

}
