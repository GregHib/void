package org.redrune.cache.config.decoder

import org.redrune.cache.Configs.HIT_SPLATS
import org.redrune.cache.config.ConfigDecoder
import org.redrune.cache.config.data.HitSplatDefinition
import org.redrune.storage.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class HitSplatDecoder : ConfigDecoder<HitSplatDefinition>(HIT_SPLATS) {

    override fun create() = HitSplatDefinition()

    override fun HitSplatDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> font = buffer.readShort()
            2 -> textColour = buffer.readMedium()
            3 -> icon = buffer.readShort()
            4 -> left = buffer.readShort()
            5 -> middle = buffer.readShort()
            6 -> right = buffer.readShort()
            7 -> offsetX = buffer.readUnsignedShort()
            8 -> amount = buffer.readString()
            9 -> duration = buffer.readShort()
            10 -> offsetY = buffer.readUnsignedShort()
            11 -> fade = 0
            12 -> comparisonType = buffer.readUnsignedByte()
            13 -> anInt3214 = buffer.readUnsignedShort()
            14 -> fade = buffer.readShort()
        }
    }
}