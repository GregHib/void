package world.gregs.voidps.tools.photobooth.render

/**
 * Converts RuneScape packed 16-bit HSL face colours to RGB. Ported from Quill's CacheColor.
 */
object CacheColor {

    fun toRgb(packed: Int): Int {
        val hue = ((packed shr 10) and 0x3F) / 64.0
        val saturation = ((packed shr 7) and 0x07) / 8.0
        val lightness = (packed and 0x7F) / 128.0
        val r: Double
        val g: Double
        val b: Double
        if (saturation == 0.0) {
            r = lightness
            g = lightness
            b = lightness
        } else {
            val q = if (lightness < 0.5) lightness * (1.0 + saturation) else lightness + saturation - lightness * saturation
            val p = 2.0 * lightness - q
            r = hueToRgb(p, q, hue + 1.0 / 3.0)
            g = hueToRgb(p, q, hue)
            b = hueToRgb(p, q, hue - 1.0 / 3.0)
        }
        return ((r * 255.0).toInt() shl 16) or ((g * 255.0).toInt() shl 8) or (b * 255.0).toInt()
    }

    private fun hueToRgb(p: Double, q: Double, t0: Double): Double {
        var t = t0
        if (t < 0.0) t += 1.0
        if (t > 1.0) t -= 1.0
        if (t < 1.0 / 6.0) return p + (q - p) * 6.0 * t
        if (t < 1.0 / 2.0) return q
        if (t < 2.0 / 3.0) return p + (q - p) * (2.0 / 3.0 - t) * 6.0
        return p
    }
}
