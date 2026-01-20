package world.gregs.voidps.cache.type.codec

import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.Type
import java.io.File

abstract class TypeCodec<T: Type> {

    abstract fun create(size: Int, block: (T) -> Unit): Array<T>

    fun read(file: File): Array<T> {
        val array = file.readBytes()
        val reader = ArrayReader(array)
        return create(reader.readInt()) {
            read(reader, it)
        }
    }

    abstract fun read(reader: Reader, type: T)

    fun write(file: File, types: Array<T>, size: Int = 100_000) {
        val writer = ArrayWriter(size)
        writer.writeInt(types.size)
        for (type in types) {
            write(writer, type)
        }
        file.writeBytes(writer.toArray())
    }

    abstract fun write(writer: Writer, definition: T)

}