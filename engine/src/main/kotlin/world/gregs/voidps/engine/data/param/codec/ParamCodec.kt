package world.gregs.voidps.engine.data.param.codec

import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

abstract class ParamCodec<T>(val id: Int) {
    abstract fun read(reader: Reader): T
    abstract fun read(reader: ConfigReader): T
    abstract fun write(writer: Writer, value: T)
    @Suppress("UNCHECKED_CAST")
    @JvmName("writeAny")
    fun write(writer: Writer, value: Any) = write(writer, value as T)
}
