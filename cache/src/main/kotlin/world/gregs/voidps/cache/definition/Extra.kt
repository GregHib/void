package world.gregs.voidps.cache.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.buffer.read.Reader

@Suppress("UNCHECKED_CAST")
interface Extra {
    var stringId: String
    var extras: Map<Int, Any>?

    operator fun <T : Any> get(key: String): T = extras!!.getValue(Params.id(key)) as T

    operator fun <T : Any> get(key: Int): T = extras!!.getValue(key) as T

    fun contains(key: Int): Boolean = extras?.containsKey(key) ?: false

    fun contains(key: String?): Boolean {
        if (key == null) {
            return false
        }
        return extras?.containsKey(Params.id(key)) ?: false
    }

    fun <T : Any> getOrNull(key: Int) = extras?.get(key) as? T

    fun <T : Any> getOrNull(key: String) = extras?.get(Params.id(key)) as? T

    operator fun <T : Any> get(key: Int, defaultValue: T) = getOrNull(key) as? T ?: defaultValue

    operator fun <T : Any> get(key: String, defaultValue: T) = getOrNull(key) as? T ?: defaultValue

    fun readParameters(buffer: Reader, parameters: Parameters) {
        val length = buffer.readUnsignedByte()
        if (length == 0) {
            return
        }
        val extras = Object2ObjectOpenHashMap<Int, Any>(4, Hash.VERY_FAST_LOAD_FACTOR)
        for (i in 0 until length) {
            val string = buffer.readUnsignedBoolean()
            val id = buffer.readUnsignedMedium()
            parameters.set(extras, id, if (string) buffer.readString() else buffer.readInt())
        }
        this.extras = extras
    }
}
