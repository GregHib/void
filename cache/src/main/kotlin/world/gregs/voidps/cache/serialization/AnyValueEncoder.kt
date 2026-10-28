package world.gregs.voidps.cache.serialization

/**
 * Implemented by encoders which can write values of types unknown to this module, such as enums from the engine.
 */
interface AnyValueEncoder {
    fun encodeAnyValue(value: Any?)
}
