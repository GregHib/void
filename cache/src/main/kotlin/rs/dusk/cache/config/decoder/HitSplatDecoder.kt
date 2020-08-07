package rs.dusk.cache.config.decoder

import rs.dusk.cache.Cache
import rs.dusk.cache.Configs.HIT_SPLATS
import rs.dusk.cache.config.ConfigDecoder
import rs.dusk.cache.config.data.HitSplatDefinition
import rs.dusk.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class HitSplatDecoder(cache: Cache) : ConfigDecoder<HitSplatDefinition>(cache, HIT_SPLATS) {

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