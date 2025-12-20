package world.gregs.voidps.cache.type.field

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

interface FieldCodec<T> {
    fun bytes(value: T): Int
    fun readBinary(reader: Reader): T
    fun writeBinary(writer: Writer, value: T)
    fun readConfig(reader: ConfigReader): T
    fun writeConfig(writer: ConfigWriter, value: T)
}