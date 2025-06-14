package world.gregs.voidps.cache.config.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Config.FLOOR_OVERLAY
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.config.data.OverlayDefinition

class OverlayDecoder : ConfigDecoder<OverlayDefinition>(FLOOR_OVERLAY) {

    override fun create(size: Int) = Array(size) { OverlayDefinition(it) }

    override fun OverlayDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> colour = calculateHsl(buffer.readUnsignedMedium())
            2 -> texture = buffer.readUnsignedByte()
            3 -> {
                texture = buffer.readShort()
                if (texture == 65535) {
                    texture = -1
                }
            }
            5 -> hideUnderlay = false
            7 -> blendColour = calculateHsl(buffer.readUnsignedMedium())
            8 -> anInt961 = id
            9 -> scale = buffer.readShort() shl 2
            10 -> blockShadow = false
            11 -> anInt3633 = buffer.readUnsignedByte()
            12 -> underlayOverrides = true
            13 -> waterColour = buffer.readUnsignedMedium()
            14 -> waterScale = buffer.readUnsignedByte() shl 2
            16 -> waterIntensity = buffer.readUnsignedByte()
        }
    }

    override fun changeValues(definitions: Array<OverlayDefinition>, definition: OverlayDefinition) {
        definition.anInt3633 = definition.id or (definition.anInt3633 shl 8)
    }

    companion object {

        private fun calculateHsl(i: Int): Int = if (i == 16711935) {
            -1
        } else {
            shiftRGBColours(i)
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
            val h2 = (256.0 * h).toInt()
            var s2 = (256.0 * s).toInt()
            var l2 = (256.0 * l).toInt()
            if (s2 < 0) {
                s2 = 0
            } else if (s2 > 255) {
                s2 = 255
            }
            if (l2 >= 0) {
                if (l2 > 255) {
                    l2 = 255
                }
            } else {
                l2 = 0
            }
            // Shift hsl
            s2 = if (l2 <= 243) {
                when {
                    l2 > 217 -> s2 shr 3
                    l2 > 192 -> s2 shr 2
                    l2 > 179 -> s2 shr 1
                    else -> s2
                }
            } else {
                s2 shr 4
            }
            return (l2 shr 1) + ((s2 shr 5 shl 7) + (h2 and 0xff shr 2 shl 10))
        }
    }
}
