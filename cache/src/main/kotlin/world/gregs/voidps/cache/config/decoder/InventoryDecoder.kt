package world.gregs.voidps.cache.config.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Config.INVENTORIES
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.config.data.InventoryDefinition

class InventoryDecoder : ConfigDecoder<InventoryDefinition>(INVENTORIES) {

    override fun create(size: Int) = Array(size) { InventoryDefinition(it) }

    override fun InventoryDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            2 -> length = buffer.readUnsignedShort()
            4 -> {
                val size = buffer.readUnsignedByte()
                ids = IntArray(size)
                amounts = IntArray(size)
                for (i in 0 until size) {
                    ids!![i] = buffer.readUnsignedShort()
                    amounts!![i] = buffer.readUnsignedShort()
                }
            }
        }
    }
}
