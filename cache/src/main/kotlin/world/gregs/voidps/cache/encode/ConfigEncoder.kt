package world.gregs.voidps.cache.encode

import world.gregs.voidps.buffer.write.Writer

class ConfigEncoder(val configs: Set<Int>) : IndexEncoder() {
    override fun encode(writer: Writer, index: Int, archive: Int, file: Int, data: ByteArray) {
        if (configs.contains(archive)) {
            writer.writeShort(file)
            super.encode(writer, index, archive, file, data)
        }
    }
}