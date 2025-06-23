package world.gregs.voidps.tools.map.render.raster

import world.gregs.voidps.tools.map.render.raster.ColourPalette.hsvColourPalette
import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Raster(private val bi: BufferedImage) {
    val width: Int = bi.width
    val height: Int = bi.height

    private var minX = 0
    private var minY = 0

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

        val used = Array(maxX - minX + 1) { BooleanArray(maxY - minY + 1) }

        this.minX = minX
        this.minY = minY

        bresenhamLine(used, x1, y1, x2, y2, colour1, colour2)
        bresenhamLine(used, x1, y1, x3, y3, colour1, colour3)
        bresenhamLine(used, x2, y2, x3, y3, colour2, colour3)

        // Find right point
        for (y in minY until maxY) {
            for (x in minX until maxX) {
                if (used[x - this.minX][y - this.minY]) {
                    val leftColour = get(x, y)
                    for (rx in maxX downTo minX) {
                        if (used[rx - this.minX][y - this.minY]) {
                            bresenhamLine(used, x, y, rx, y, leftColour, get(rx, y))
                            break
                        }
                    }
                    break
                }
            }
        }
    }

    private fun bresenhamLine(used: Array<BooleanArray>, x0: Int, y0: Int, x1: Int, y1: Int, colour1: Int, colour2: Int) {
        var x0 = x0
        var y0 = y0

        val deltaWidth = x1 - x0
        val deltaHeight = y1 - y0

        var dx0 = 0
        var dy0 = 0
        var dx1 = 0
        var dy1 = 0

        if (deltaWidth < 0) {
            dx0 = -1
        } else if (deltaWidth > 0) {
            dx0 = 1
        }
        if (deltaHeight < 0) {
            dy0 = -1
        } else if (deltaHeight > 0) {
            dy0 = 1
        }
        if (deltaWidth < 0) {
            dx1 = -1
        } else if (deltaWidth > 0) {
            dx1 = 1
        }

        var longest = abs(deltaWidth)
        var shortest = abs(deltaHeight)

        if (longest <= shortest) {
            longest = abs(deltaHeight)
            shortest = abs(deltaWidth)
            if (deltaHeight < 0) {
                dy1 = -1
            } else if (deltaHeight > 0) {
                dy1 = 1
            }
            dx1 = 0
        }
        var numerator = longest shr 1
        val red1 = colour1 shr 16 and 0xff
        val green1 = colour1 shr 8 and 0xff
        val blue1 = colour1 and 0xff
        val red2 = colour2 shr 16 and 0xff
        val green2 = colour2 shr 8 and 0xff
        val blue2 = colour2 and 0xff

        val rStep = (red2 - red1).toFloat() / longest
        val gStep = (green2 - green1).toFloat() / longest
        val bStep = (blue2 - blue1).toFloat() / longest

        for (i in 0..longest) {
            // Colour computation
            val r = (red1 + rStep * i).toDouble()
            val g = (green1 + gStep * i).toDouble()
            val b = (blue1 + bStep * i).toDouble()

            // Pixel writing
            if (y0 > -1 && x0 > -1 && y0 < height && x0 < width) {
                val pixel = (abs(r).toInt() shl 16 or (abs(g).toInt() shl 8) or abs(b).toInt())
                if (pixel > 0) {
                    set(x0, y0, -16777216 or pixel)
                }
                used[x0 - minX][y0 - minY] = true
            }

            numerator += shortest
            if (numerator >= longest) {
                numerator -= longest
                x0 += dx0
                y0 += dy0
            } else {
                x0 += dx1
                y0 += dy1
            }
        }
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
