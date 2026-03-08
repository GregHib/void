package world.gregs.voidps.cache.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.definition.Params.CATEGORY
import world.gregs.voidps.cache.definition.Params.MAGIC_STRENGTH
import world.gregs.voidps.cache.definition.Params.RANGED_STRENGTH
import world.gregs.voidps.cache.definition.Params.STRENGTH

@Suppress("UNCHECKED_CAST")
interface Parameterized {
    var stringId: String
    var params: Map<Int, Any>?

    operator fun <T : Any> get(key: String): T = params!!.getValue(Params.id(key)) as T

    operator fun <T : Any> get(key: Int): T = params!!.getValue(key) as T

    fun contains(key: Int): Boolean = params?.containsKey(key) ?: false

    fun contains(key: String?): Boolean {
        if (key == null) {
            return false
        }
        return params?.containsKey(Params.id(key)) ?: false
    }

    fun <T : Any> getOrNull(key: Int) = params?.get(key) as? T

    fun <T : Any> getOrNull(key: String) = params?.get(Params.idOrNull(key)) as? T

    operator fun <T : Any> get(key: Int, defaultValue: T) = getOrNull(key) as? T ?: defaultValue

    operator fun <T : Any> get(key: String, defaultValue: T) = getOrNull(key) as? T ?: defaultValue

    fun readParameters(buffer: Reader) {
        val length = buffer.readUnsignedByte()
        if (length == 0) {
            return
        }
        val params = Int2ObjectOpenHashMap<Any>(4, Hash.VERY_FAST_LOAD_FACTOR)
        for (i in 0 until length) {
            val string = buffer.readUnsignedBoolean()
            val id = buffer.readUnsignedMedium()
            val value = if (string) buffer.readString() else buffer.readInt()
            when (id) {
                RANGED_STRENGTH, MAGIC_STRENGTH, STRENGTH -> params[id] = (value as Int) / 10.0
                CATEGORY -> {
                    val set = ObjectOpenHashSet<String>()
                    val int = value as Int
                    set.add(Category.name(int))
                    params[Params.CATEGORIES] = set
                }
                else -> params[id] = value
            }
        }
        this.params = params
    }

    fun writeParameters(writer: Writer) {
        params?.let { params ->
            writer.writeByte(249)
            writer.writeByte(params.size)
            params.forEach { (id, value) ->
                writer.writeByte(value is String)
                writer.writeMedium(id)
                if (value is String) {
                    writer.writeString(value)
                } else if (value is Int) {
                    writer.writeInt(value)
                }
            }
        }
    }
}
