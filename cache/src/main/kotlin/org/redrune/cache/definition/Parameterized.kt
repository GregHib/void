package org.redrune.cache.definition

import org.redrune.storage.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
interface Parameterized {

    var params: HashMap<Long, Any>?

    fun readParameters(buffer: Reader) {
        val length = buffer.readUnsignedByte()
        if (length == 0) {
            return
        }
        val capacity = calculateCapacity(length)
        params = HashMap(capacity)
        repeat(length) {
            val string = buffer.readUnsignedBoolean()
            val id = buffer.readMedium().toLong()
            params!![id] = if (string) buffer.readString() else buffer.readInt()
        }
    }

    companion object {
        internal fun calculateCapacity(initial: Int): Int {
            var i = initial - 1
            i = i or i ushr 1
            i = i or i ushr 2
            i = i or i ushr 4
            i = i or i ushr 8
            i = i or i ushr 16
            return 1 + i
        }
    }
}