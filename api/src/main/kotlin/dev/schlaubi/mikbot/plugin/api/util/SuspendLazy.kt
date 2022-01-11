package dev.schlaubi.mikbot.plugin.api.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

/**
 * Similar to [Lazy] but for suspend functions.
 *
 * ```kotlin
 * val suspendLazy = suspendLazy { suspendCall() }
 *
 * suspendLazy() // retrieve
 * ```
 *
 * @see suspendLazy
 */
public interface SuspendLazy<out T> {
    public suspend fun get(): T

    public fun isInitialized(): Boolean

    public suspend operator fun invoke(): T = get()
}

/**
 * @see SuspendLazy
 */
public fun <T> suspendLazy(initializer: suspend () -> T): SuspendLazy<T> = SynchronizedSuspendLazyImpl(initializer)

/**
 * The same as [suspendLazy] but with [LazyThradSafetyMode.NONE]
 *
 * Use this for local lazy variables, which only one thread/coroutine can access
 */
public fun <T> localSuspendLazy(initializer: suspend () -> T): SuspendLazy<T> = UnsafeSuspendLazyImpl(initializer)

/**
 * @see SuspendLazy
 */
public fun <T> suspendLazy(mode: LazyThreadSafetyMode, initializer: suspend () -> T): SuspendLazy<T> =
    when (mode) {
        LazyThreadSafetyMode.SYNCHRONIZED -> SynchronizedSuspendLazyImpl(initializer)
        LazyThreadSafetyMode.PUBLICATION -> SafePublicationSuspendLazyImpl(initializer)
        LazyThreadSafetyMode.NONE -> UnsafeSuspendLazyImpl(initializer)
    }

@Suppress("ClassName")
private object UNINITIALIZED_VALUE

private class SynchronizedSuspendLazyImpl<out T>(initializer: suspend () -> T, lock: Mutex? = null) : SuspendLazy<T> {
    private var initializer: (suspend () -> T)? = initializer

    @Volatile
    private var _value: Any? = UNINITIALIZED_VALUE

    // final field is required to enable safe publication of constructed instance
    private val lock = lock ?: Mutex()

    override suspend fun get(): T {
        val _v1 = _value
        if (_v1 !== UNINITIALIZED_VALUE) {
            @Suppress("UNCHECKED_CAST")
            return _v1 as T
        }

        return lock.withLock {
            val _v2 = _value
            if (_v2 !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST") (_v2 as T)
            } else {
                val typedValue = initializer!!.invoke()
                _value = typedValue
                initializer = null
                typedValue
            }
        }
    }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun toString(): String = if (isInitialized()) _value.toString() else "Lazy value not initialized yet."
}

private class SafePublicationSuspendLazyImpl<out T>(initializer: suspend () -> T) : SuspendLazy<T> {
    @Volatile
    private var initializer: (suspend () -> T)? = initializer

    @Volatile
    private var _value: Any? = UNINITIALIZED_VALUE

    // this final field is required to enable safe initialization of the constructed instance
    private val final: Any = UNINITIALIZED_VALUE

    override suspend fun get(): T {
        val value = _value
        if (value !== UNINITIALIZED_VALUE) {
            @Suppress("UNCHECKED_CAST")
            return value as T
        }

        val initializerValue = initializer
        // if we see null in initializer here, it means that the value is already set by another thread
        if (initializerValue != null) {
            val newValue = initializerValue()
            if (valueUpdater.compareAndSet(this, UNINITIALIZED_VALUE, newValue)) {
                initializer = null
                return newValue
            }
        }
        @Suppress("UNCHECKED_CAST")
        return _value as T
    }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun toString(): String = if (isInitialized()) _value.toString() else "Lazy value not initialized yet."

    companion object {
        private val valueUpdater = AtomicReferenceFieldUpdater.newUpdater(
            SafePublicationSuspendLazyImpl::class.java,
            Any::class.java,
            "_value"
        )
    }
}

private class UnsafeSuspendLazyImpl<out T>(initializer: suspend () -> T) : SuspendLazy<T> {
    private var initializer: (suspend () -> T)? = initializer
    private var _value: Any? = UNINITIALIZED_VALUE

    override suspend fun get(): T {
        if (_value === UNINITIALIZED_VALUE) {
            _value = initializer!!()
            initializer = null
        }
        @Suppress("UNCHECKED_CAST")
        return _value as T
    }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun toString(): String = if (isInitialized()) _value.toString() else "Lazy value not initialized yet."
}
