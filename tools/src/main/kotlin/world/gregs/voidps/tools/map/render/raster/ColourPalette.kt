package world.gregs.voidps.tools.map.render.raster

import kotlin.math.pow

object ColourPalette {
    val hsvColourPalette = IntArray(65536)

    init {
        val intensity = 0.7 + (-0.015 + 0.03 * 1)
        var index = 0
        var colour = 0
        while (colour < 512) {
            val hue = ((colour shr 3).toFloat() / 64.0f + 0.0078125f) * 360.0f
            val saturation = 0.0625f + (0x7 and colour).toFloat() / 8.0f

            for (brightness in 0..127) {
                val value = brightness.toFloat() / 128.0f
                var normalisedRed = 0.0f
                var normalisedGreen = 0.0f
                var normalisedBlue = 0.0f
                val f10 = hue / 60.0f
                val i11 = f10.toInt()
                val type = i11 % 6
                val f13 = f10 - i11.toFloat()
                val chroma = value * (-saturation + 1.0f)
                val f15 = value * (-(f13 * saturation) + 1.0f)
                val f16 = (1.0f - saturation * (-f13 + 1.0f)) * value
                when (type) {
                    0 -> {
                        normalisedRed = value
                        normalisedBlue = chroma
                        normalisedGreen = f16
                    }
                    1 -> {
                        normalisedBlue = chroma
                        normalisedRed = f15
                        normalisedGreen = value
                    }
                    2 -> {
                        normalisedRed = chroma
                        normalisedGreen = value
                        normalisedBlue = f16
                    }
                    3 -> {
                        normalisedGreen = f15
                        normalisedRed = chroma
                        normalisedBlue = value
                    }
                    4 -> {
                        normalisedBlue = value
                        normalisedRed = f16
                        normalisedGreen = chroma
                    }
                    5 -> {
                        normalisedGreen = chroma
                        normalisedBlue = f15
                        normalisedRed = value
                    }
                }
                normalisedRed = normalisedRed.toDouble().pow(intensity).toFloat()
                normalisedGreen = normalisedGreen.toDouble().pow(intensity).toFloat()
                normalisedBlue = normalisedBlue.toDouble().pow(intensity).toFloat()
                val r = (normalisedRed * 256.0f).toInt()
                val g = (normalisedGreen * 256.0f).toInt()
                val b = (normalisedBlue * 256.0f).toInt()
                val rgb = (g shl 8) + -16777216 + ((r shl 16) + b)
                hsvColourPalette[index++] = rgb
            }
            colour++
        }
    }

    fun hslToPaletteIndex(luminance: Int, saturation: Int, hue: Int): Int {
        var saturation = saturation
        if (luminance <= 243) {
            if (luminance <= 217) {
                if (luminance > 192) {
                    saturation = saturation shr 2
                } else if (luminance > 179) {
                    saturation = saturation shr 1
                }
            } else {
                saturation = saturation shr 3
            }
        } else {
            saturation = saturation shr 4
        }
        return (luminance shr 1) + (saturation shr 5 shl 7) + (0xff and hue shr 2 shl 10)
    }
}
