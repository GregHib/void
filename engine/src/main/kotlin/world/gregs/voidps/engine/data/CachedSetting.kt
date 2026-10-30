package world.gregs.voidps.engine.data

import kotlin.reflect.KProperty

/**
 * A pre-parsed [Settings] value for hot paths, re-parsed by [Settings] whenever its properties change.
 * Settings holds every instance forever so only create them as top-level, object or companion fields.
 */
class CachedSetting<T>(
    val name: String,
    private val default: T,
    private val parse: (String) -> T?,
) {
    var value: T = default
        private set

    internal fun refresh(settings: Settings) {
        value = settings.getOrNull(name)?.let(parse) ?: default
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}
