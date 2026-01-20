package world.gregs.voidps.cache.type

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

interface Type {
    val id: Int

    fun decode(reader: Reader)

    fun encode(writer: Writer)
}
