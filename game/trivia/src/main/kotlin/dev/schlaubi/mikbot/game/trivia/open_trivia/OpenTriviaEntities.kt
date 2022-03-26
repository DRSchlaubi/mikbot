package dev.schlaubi.mikbot.game.trivia.open_trivia

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import dev.schlaubi.mikbot.game.multiple_choice.Question as GameQuestion

@Serializable
data class OpenTriviaResponse(
    @SerialName("response_code") val responseCode: Int,
    @SerialName("response_message") val responseMessage: String? = null,
    val results: List<Question> = emptyList(),
    val token: String? = null
)

@Serializable
data class Question(
    val category: Category,
    val type: Type,
    val difficulty: Difficulty,
    @SerialName("question") override val title: String,
    @SerialName("correct_answer") override val correctAnswer: String,
    @SerialName("incorrect_answers") override val incorrectAnswers: List<String>,
    private val sortedAnswers: List<String>? = null
) : GameQuestion {
    override val allAnswers: List<String>
        get() = sortedAnswers
            ?: if (type == Type.MULTIPLE_CHOICE) super.allAnswers.shuffled() else super.allAnswers.sortedByDescending {
                it.equals(
                    "true", ignoreCase = true
                )
            }
}

@Serializable
enum class Type(val translationName: String) : ChoiceEnum {

    @SerialName("boolean")
    TRUE_FALSE("true_false"),

    @SerialName("multiple")
    MULTIPLE_CHOICE("multiple_choice");

    override val readableName: String
        get() = "trivia.question.type.$translationName"
}

@Serializable
enum class Difficulty(val translationName: String) : ChoiceEnum {
    @SerialName("easy")
    EASY("easy"),

    @SerialName("medium")
    MEDIUM("medium"),

    @SerialName("hard")
    HARD("hard");

    override val readableName: String
        get() = "trivia.question.type.$translationName"
}

@Serializable(with = Category.Serializer::class)
enum class Category(val id: Int, val apiName: String, val translationName: String) : ChoiceEnum {
    GENERAL_KNOWLEDGE(9, "General Knowledge", "general_knowledge"), ENTERTAINMENT_BOOKS(
        10,
        "Entertainment: Books",
        "entertainment_books"
    ),
    ENTERTAINMENT_FILM(11, "Entertainment: Film", "entertainment_film"), ENTERTAINMENT_MUSIC(
        12,
        "Entertainment: Music",
        "entertainment_music"
    ),
    ENTERTAINMENT_MUSICALS_THEATRES(
        13,
        "Entertainment: Musicals & Theatres",
        "entertainment_musicals_theatres"
    ),
    ENTERTAINMENT_TELEVISION(14, "Entertainment: Television", "entertainment_television"), ENTERTAINMENT_VIDEO_GAMES(
        15,
        "Entertainment: Video Games",
        "entertainment_video_games"
    ),
    ENTERTAINMENT_BOARD_GAMES(16, "Entertainment: Board Games", "entertainment_board_games"), SCIENCE_NATURE(
        17,
        "Science & Nature",
        "science_nature"
    ),
    SCIENCE_COMPUTERS(18, "Science: Computers", "science_computers"), SCIENCE_MATHEMATICS(
        19,
        "Science: Mathematics",
        "science_mathematics"
    ),
    MYTHOLOGY(20, "Mythology", "mythology"), SPORTS(21, "Sports", "sports"), GEOGRAPHY(
        22,
        "Geography",
        "geography"
    ),
    HISTORY(23, "History", "history"), POLITICS(24, "Politics", "politics"), ART(25, "Art", "art"), CELEBRITIES(
        26,
        "Celebrities",
        "celebrities"
    ),
    ANIMALS(27, "Animals", "animals"), VEHICLES(28, "Vehicles", "vehicles"), ENTERTAINMENT_COMICS(
        29,
        "Entertainment: Comics",
        "entertainment_comics"
    );

    override val readableName: String
        get() = "trivia.question.category.$translationName"

    companion object Serializer : KSerializer<Category> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Category", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Category) {
            encoder.encodeString(value.apiName)
        }

        override fun deserialize(decoder: Decoder): Category {
            val apiName = decoder.decodeString()
            return values().firstOrNull { it.apiName == apiName } ?: GENERAL_KNOWLEDGE
        }
    }
}
