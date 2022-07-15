package dev.schlaubi.mikbot.plugin.api.util

@RequiresOptIn(message = "You could be using this API wrong. Only use it when you know what you're doing!")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY)
public annotation class IKnowWhatIAmDoing
