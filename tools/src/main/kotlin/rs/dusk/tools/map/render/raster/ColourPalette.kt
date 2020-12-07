package rs.dusk.tools.map.render.raster

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
                val f_10_ = hue / 60.0f
                val i_11_ = f_10_.toInt()
                val type = i_11_ % 6
                val f_13_ = f_10_ - i_11_.toFloat()
                val chroma = value * (-saturation + 1.0f)
                val f_15_ = value * (-(f_13_ * saturation) + 1.0f)
                val f_16_ = (1.0f - saturation * (-f_13_ + 1.0f)) * value
                when (type) {
                    0 -> {
                        normalisedRed = value
                        normalisedBlue = chroma
                        normalisedGreen = f_16_
                    }
                    1 -> {
                        normalisedBlue = chroma
                        normalisedRed = f_15_
                        normalisedGreen = value
                    }
                    2 -> {
                        normalisedRed = chroma
                        normalisedGreen = value
                        normalisedBlue = f_16_
                    }
                    3 -> {
                        normalisedGreen = f_15_
                        normalisedRed = chroma
                        normalisedBlue = value
                    }
                    4 -> {
                        normalisedBlue = value
                        normalisedRed = f_16_
                        normalisedGreen = chroma
                    }
                    5 -> {
                        normalisedGreen = chroma
                        normalisedBlue = f_15_
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
}