package dev.schlaubi.mikbot.plugin.api

/**
 * **DO NOT USE THIS API IN PLUGINS!!**
 */
@MustBeDocumented
@RequiresOptIn
@Retention(AnnotationRetention.BINARY)
public annotation class InternalAPI


/**
 * Class used to mark the main class of a plugin.
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class PluginMain
