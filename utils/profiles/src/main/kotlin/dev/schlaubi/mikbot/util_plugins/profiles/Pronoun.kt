package dev.schlaubi.mikbot.util_plugins.profiles

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import kotlinx.serialization.Serializable

@Serializable
enum class Pronoun(
    val displayName: String,
    val firstPerson: String,
    val thirdPerson: String,
    val url: String,
    englishDisplayName: String
) : ChoiceEnum {

    SHE_HER(
        "profiles.pronouns.she_her",
        "profiles.pronouns.she_first_person",
        "profiles.pronouns.she_third_person",
        "https://pronoun.is/she/her",
        "She/Her"
    ),
    HE_HIM(
        "profiles.pronouns.he_him",
        "profiles.pronouns.he_first_person",
        "profiles.pronouns.he_third_person",
        "https://pronoun.is/he/him",
        "He/Him"
    ),
    THEY_THEM(
        "profiles.pronouns.they_them",
        "profiles.pronouns.they_first_person",
        "profiles.pronouns.they_third_person",
        "https://pronoun.is/they/them",
        "They/Them"
    );

    override val readableName: String = englishDisplayName
}
