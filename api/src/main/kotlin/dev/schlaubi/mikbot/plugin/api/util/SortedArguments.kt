package dev.schlaubi.mikbot.plugin.api.util

import com.kotlindiscord.kord.extensions.annotations.UnexpectedFunctionBehaviour
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.*

/**
 * This class sorts its arguments by the [Converter.required] property, ensuring that required arguments always come
 * before non-required ones.
 *
 * **NOTICE:** Please only use this for slash commands and when you are sure that you are aware of the argument order
 * being changed in a possibly unpredictable way
 */
@OptIn(UnexpectedFunctionBehaviour::class)
@IKnowWhatIAmDoing
public abstract class SortedArguments : Arguments() {
    public override fun <R : Any> arg(
        displayName: String,
        description: String,
        converter: SingleConverter<R>,
    ): SingleConverter<R> = addArgument(displayName, description, converter)

    public override fun <R : Any> arg(
        displayName: String,
        description: String,
        converter: DefaultingConverter<R>,
    ): DefaultingConverter<R> = addArgument(displayName, description, converter)

    public override fun <R : Any> arg(
        displayName: String,
        description: String,
        converter: OptionalConverter<R>,
    ): OptionalConverter<R> = addArgument(displayName, description, converter)

    public override fun <R : Any> arg(
        displayName: String,
        description: String,
        converter: ListConverter<R>,
    ): ListConverter<R> =addArgument(displayName, description, converter)

    public override fun <R : Any> arg(
        displayName: String,
        description: String,
        converter: CoalescingConverter<R>,
    ): CoalescingConverter<R> =addArgument(displayName, description, converter)

    public override fun <R : Any> arg(
        displayName: String,
        description: String,
        converter: DefaultingCoalescingConverter<R>,
    ): DefaultingCoalescingConverter<R> =addArgument(displayName, description, converter)

    public override fun <R : Any> arg(
        displayName: String,
        description: String,
        converter: OptionalCoalescingConverter<R>,
    ): OptionalCoalescingConverter<R> = addArgument(displayName, description, converter)


    private fun <C : Converter<*, *, *, *>> addArgument(
        displayName: String,
        description: String,
        converter: C,
    ): C {
        val argument = Argument(displayName, description, converter)
        val index = if (argument.converter.required && !args.last().converter.required) {
            args.indexOfLast { it.converter.required }.coerceAtLeast(0)
        } else {
            args.size
        }
        args.add(index, argument)
        return converter
    }
}
