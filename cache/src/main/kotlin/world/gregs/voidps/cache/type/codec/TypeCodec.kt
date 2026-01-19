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
        val reader = ArrayReader(file.readBytes())
        return create(reader.readInt()) {
            read(it, reader)
        }
    }

    protected abstract fun read(type: T, reader: Reader)

    fun write(file: File, types: Array<T>, size: Int = 100_000) {
        val writer = ArrayWriter(size)
        for (type in types) {
            write(type, writer)
        }
        file.writeBytes(writer.toArray())
    }

    protected abstract fun write(definition: T, writer: Writer)

}