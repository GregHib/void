package rs.dusk.cache.definition

import rs.dusk.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
@Suppress("UNCHECKED_CAST")
interface Parameterized {

    var params: HashMap<Long, Any>?

    fun <T : Any> getParam(key: Long) = params?.get(key) as T

    fun <T : Any> getParamOrNull(key: Long) = params?.get(key) as? T

    fun <T : Any> getParam(key: Long, default: T) = params?.get(key) as? T ?: default

    fun readParameters(buffer: Reader) {
        val length = buffer.readUnsignedByte()
        if (length == 0) {
            return
        }
        params = HashMap()
        repeat(length) {
            val string = buffer.readUnsignedBoolean()
            val id = buffer.readUnsignedMedium().toLong()
            params!![id] = if (string) buffer.readString() else buffer.readInt()
        }
    }
}