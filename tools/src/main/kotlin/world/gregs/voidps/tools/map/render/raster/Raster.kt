package world.gregs.voidps.tools.map.render.raster

import world.gregs.voidps.tools.map.render.raster.ColourPalette.hsvColourPalette
import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

class Raster(private val bi: BufferedImage) {
    val width: Int = bi.width
    val height: Int = bi.height

    fun get(x: Int, y: Int): Int = bi.getRGB(x, y)

    fun set(x: Int, y: Int, value: Int) {
        bi.setRGB(x, y, value)
    }

    fun drawGouraudTriangle(y1: Int, y2: Int, y3: Int, x1: Int, x2: Int, x3: Int, colour1: Int, colour2: Int, colour3: Int) {
        drawGouraud(x1, x2, x3, y1, y2, y3, hsvColourPalette[colour1], hsvColourPalette[colour2], hsvColourPalette[colour3])
    }

    private fun drawGouraud(x1: Int, x2: Int, x3: Int, y1: Int, y2: Int, y3: Int, colour1: Int, colour2: Int, colour3: Int) {
        val minX = min(x1, min(x2, x3))
        val maxX = max(x1, max(x2, x3))
        val minY = min(y1, min(y2, y3))
        val maxY = max(y1, max(y2, y3))

        val area = (x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1)
        if (area == 0) {
            return
        }

        val exclude = if (area > 0) -1 else 1
        val bias1 = edgeBias(x2, y2, x3, y3, area, exclude)
        val bias2 = edgeBias(x3, y3, x1, y1, area, exclude)
        val bias3 = edgeBias(x1, y1, x2, y2, area, exclude)

        val r1 = colour1 shr 16 and 0xff
        val g1 = colour1 shr 8 and 0xff
        val b1 = colour1 and 0xff
        val r2 = colour2 shr 16 and 0xff
        val g2 = colour2 shr 8 and 0xff
        val b2 = colour2 and 0xff
        val r3 = colour3 shr 16 and 0xff
        val g3 = colour3 shr 8 and 0xff
        val b3 = colour3 and 0xff

        val areaF = area.toFloat()
        for (y in minY..maxY) {
            if (y !in 0..<height) {
                continue
            }
            for (x in minX..maxX) {
                if (x !in 0..<width) {
                    continue
                }
                val e1 = (x3 - x2) * (y - y2) - (y3 - y2) * (x - x2)
                val e2 = (x1 - x3) * (y - y3) - (y1 - y3) * (x - x3)
                val e3 = (x2 - x1) * (y - y1) - (y2 - y1) * (x - x1)
                if (area > 0) {
                    if (e1 + bias1 < 0 || e2 + bias2 < 0 || e3 + bias3 < 0) {
                        continue
                    }
                } else {
                    if (e1 + bias1 > 0 || e2 + bias2 > 0 || e3 + bias3 > 0) {
                        continue
                    }
                }
               val w1 = e1 / areaF
                val w2 = e2 / areaF
                val w3 = 1f - w1 - w2
                val r = (w1 * r1 + w2 * r2 + w3 * r3).toInt().coerceIn(0, 255)
                val g = (w1 * g1 + w2 * g2 + w3 * g3).toInt().coerceIn(0, 255)
                val b = (w1 * b1 + w2 * b2 + w3 * b3).toInt().coerceIn(0, 255)
                set(x, y, -16777216 or (r shl 16) or (g shl 8) or b)
            }
        }
    }

    private fun edgeBias(ax: Int, ay: Int, bx: Int, by: Int, area: Int, exclude: Int): Int {
        val dx = bx - ax
        val dy = by - ay
        val topLeft = if (area > 0) {
            dy < 0 || (dy == 0 && dx < 0)
        } else {
            dy > 0 || (dy == 0 && dx > 0)
        }
        return if (topLeft) 0 else exclude
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val bi = BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB)
            val raster = Raster(bi)
            val one = Point(0, 0)
            val two = Point(0, 100)
            val three = Point(100, 0)
            raster.drawGouraudTriangle(one.x, two.x, three.x, one.y, two.y, three.y, Color.RED.rgb, Color.GREEN.rgb, Color.BLUE.rgb)

            ImageIO.write(bi, "png", File("saved.png"))
        }
    }
}
