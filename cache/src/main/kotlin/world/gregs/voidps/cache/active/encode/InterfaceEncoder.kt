package world.gregs.voidps.cache.active.encode

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.active.ActiveIndexEncoder
import world.gregs.voidps.cache.definition.data.InterfaceDefinition

class InterfaceEncoder : ActiveIndexEncoder(Index.INTERFACES) {

    override fun encode(writer: Writer, index: Int, archive: Int, file: Int, data: ByteArray) {
        writer.writeInt(InterfaceDefinition.pack(archive, file))
        super.encode(writer, index, archive, file, data)
    }
}