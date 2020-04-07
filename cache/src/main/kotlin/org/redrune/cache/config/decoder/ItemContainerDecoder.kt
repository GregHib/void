package org.redrune.cache.config.decoder

import org.redrune.cache.Configs.CONTAINERS
import org.redrune.cache.config.ConfigDecoder
import org.redrune.cache.config.data.ItemContainerDefinition
import org.redrune.storage.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class ItemContainerDecoder : ConfigDecoder<ItemContainerDefinition>(CONTAINERS) {

    override fun create() = ItemContainerDefinition()

    override fun ItemContainerDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            2 -> length = buffer.readUnsignedShort()
            4 -> {
                val size = buffer.readUnsignedByte()
                ids = IntArray(size)
                amounts = IntArray(size)
                repeat(size) { i ->
                    ids!![i] = buffer.readUnsignedShort()
                    amounts!![i] = buffer.readUnsignedShort()
                }
            }
        }
    }
}