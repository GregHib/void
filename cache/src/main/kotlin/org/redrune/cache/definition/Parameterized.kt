package org.redrune.cache.definition

import org.redrune.core.io.read.Reader

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
        params = HashMap()
        repeat(length) {
            val string = buffer.readUnsignedBoolean()
            val id = buffer.readMedium().toLong()
            params!![id] = if (string) buffer.readString() else buffer.readInt()
        }
    }
}