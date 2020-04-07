package org.redrune.cache.config.decoder

import org.redrune.cache.Configs.FLOOR_OVERLAY
import org.redrune.cache.config.ConfigDecoder
import org.redrune.cache.config.data.OverlayDefinition
import org.redrune.storage.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class OverlayDecoder : ConfigDecoder<OverlayDefinition>(FLOOR_OVERLAY) {

    override fun create() = OverlayDefinition()

    override fun OverlayDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> colour = calculateHsl(buffer.readMedium())
            2 -> texture = buffer.readUnsignedByte()
            3 -> {
                texture = buffer.readShort()
                if (texture == 65535) {
                    texture = -1
                }
            }
            5 -> hideUnderlay = false
            7 -> blendColour = calculateHsl(buffer.readMedium())
            8 -> anInt961 = id
            9 -> scale = buffer.readShort() shl 2
            10 -> blockShadow = false
            11 -> anInt3633 = buffer.readUnsignedByte()
            12 -> underlayOverrides = true
            13 -> waterColour = buffer.readMedium()
            14 -> waterScale = buffer.readUnsignedByte() shl 2
            16 -> waterIntensity = buffer.readUnsignedByte()
        }
    }

    override fun OverlayDefinition.changeValues() {
        anInt3633 = id or (anInt3633 shl 8)
    }

    companion object {

        private fun calculateHsl(i: Int): Int {
            return if (i == 16711935) {
                -1
            } else shiftRGBColours(i)
        }

        private fun shiftRGBColours(rgb: Int): Int {
            val r = (rgb shr 16 and 0xff).toDouble() / 256.0
            val g = (0xfff2 and rgb shr 8).toDouble() / 256.0
            val b = (0xff and rgb).toDouble() / 256.0
            var maximum = r
            if (maximum > g) {
                maximum = g
            }
            if (maximum > b) {
                maximum = b
            }
            var minimum = r
            if (g > minimum) {
                minimum = g
            }
            if (b > minimum) {
                minimum = b
            }
            var h = 0.0
            var s = 0.0
            val l = (maximum + minimum) / 2.0
            if (minimum != maximum) {
                if (l < 0.5) {
                    s = (-maximum + minimum) / (maximum + minimum)
                }
                h = when (minimum) {
                    r -> (g - b) / (-maximum + minimum)
                    g -> (-r + b) / (minimum - maximum) + 2.0
                    b -> (r - g) / (minimum - maximum) + 4.0
                    else -> h
                }
                if (l >= 0.5) {
                    s = (-maximum + minimum) / (-minimum + 2.0 - maximum)
                }
            }
            h /= 6.0
            val i_15_ = (h * 256.0).toInt()
            var i_16_ = (256.0 * s).toInt()
            var i_17_ = (l * 256.0).toInt()
            if (i_16_ < 0) {
                i_16_ = 0
            } else if (i_16_ > 255) {
                i_16_ = 255
            }
            if (i_17_ >= 0) {
                if (i_17_ > 255) {
                    i_17_ = 255
                }
            } else {
                i_17_ = 0
            }
            //Shift hsl
            i_16_ = if (i_17_ <= 243) {
                when {
                    i_17_ > 217 -> i_16_ shr 3
                    i_17_ > 192 -> i_16_ shr 2
                    i_17_ > 179 -> i_16_ shr 1
                    else -> i_16_
                }
            } else {
                i_16_ shr 4
            }
            return (i_17_ shr 1) + ((i_16_ shr 5 shl 7) + (i_15_ and 0xff shr 2 shl 10))
        }
    }
}