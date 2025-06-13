package world.gregs.voidps.tools.map.render

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import world.gregs.voidps.type.Region
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * A hacky script which generates some images from [WorldMapDumper] into zoom levels which roughly works with the
 * official explv with map offsets. [MapZoomImageGenerator] is much better though.
 */
object ExplvMapGenerator {

    fun scaleImagePixelPerfect(img: BufferedImage, width: Int, height: Int): BufferedImage {
        val out = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = out.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
        g.drawImage(img, 0, 0, width, height, null)
        g.dispose()
        return out
    }

    fun multiStepScaleDown(src: BufferedImage, targetWidth: Int, targetHeight: Int): BufferedImage {
        var currentImage = src
        var w = src.width
        var h = src.height

        while (w / 2 > targetWidth && h / 2 > targetHeight) {
            w /= 2
            h /= 2
            val temp = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
            val g2 = temp.createGraphics()
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            g2.drawImage(currentImage, 0, 0, w, h, null)
            g2.dispose()
            currentImage = temp
        }

        // Final scale to exact target
        val finalImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
        val g2 = finalImage.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g2.drawImage(currentImage, 0, 0, targetWidth, targetHeight, null)
        g2.dispose()

        return finalImage
    }

    private const val LIVE = false

    suspend fun main() = coroutineScope {
        for (level in 0 until 4) {
            val inputDir = File("./images/$level/") // Files named 12345.png, based on id(x,y)
            val outputDir = if (LIVE) File("../void-map-tiles/$level/") else File("../void-map/map_tiles/$level/")
            val sourceZoom = 8
            val minZoom = 6
            val maxZoom = 9

            val inputFiles = inputDir.listFiles { _, name -> name.endsWith(".png") } ?: return@coroutineScope
            val sourceTileMap = inputFiles.mapNotNull { file ->
                val tileId = file.nameWithoutExtension.toIntOrNull() ?: return@mapNotNull null
                val sx = Region.x(tileId)
                val sy = Region.y(tileId)
                (sx to sy) to file
            }.toMap()

            val sourceCoords = sourceTileMap.keys
            val semaphore = Semaphore(8)

            // Zoom-in (source to higher levels)
            sourceCoords.map { (sx, sy) ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        val img = ImageIO.read(sourceTileMap[sx to sy]!!) ?: return@withPermit
                        try {
                            for (zoom in sourceZoom..maxZoom) {
                                val scale = 1 shl (zoom - sourceZoom)
                                val (offsetX, offsetY) = when (zoom) {
                                    9 -> 32 to 39
                                    8 -> 16 to 19
                                    else -> 0 to 0
                                }
                                val scaledImg = scaleImagePixelPerfect(img, 256 * scale, 256 * scale)
                                for (dx in 0 until scale) {
                                    for (dy in 0 until scale) {
                                        val tx = sx * scale + dx
                                        var ty = sy * scale + dy
                                        if (zoom == 9 && ty.rem(2) == 0) {
                                            ty += 2
                                        }
                                        val target = File(outputDir, "$zoom/${tx - offsetX}/${ty - offsetY}.png")
                                        if (!LIVE || target.exists()) {
                                            val tile = scaledImg.getSubimage(dx * 256, dy * 256, 256, 256)
                                            if (!LIVE) {
                                                target.parentFile.mkdirs()
                                            }
                                            ImageIO.write(tile, "png", target)
                                        } else {
                                            println("Missing file $target")
                                        }
                                    }
                                }
                                scaledImg.flush()
                            }
                        } finally {
                            img.flush()
                        }
                    }
                }
            }.awaitAll()

            // Zoom-out (combine multiple source tiles)
            for (zoom in minZoom until sourceZoom) {
                val zoomShift = sourceZoom - zoom
                val scale = 1 shl zoomShift
                val processed = mutableSetOf<Pair<Int, Int>>()

                sourceCoords.mapNotNull { (sx, sy) ->
                    val tx = sx / scale
                    val ty = sy / scale
                    if (!processed.add(tx to ty)) return@mapNotNull null
                    tx to ty
                }.map { (tx, ty) ->
                    async(Dispatchers.IO) {
                        semaphore.withPermit {
                            val composite = BufferedImage(256 * scale, 256 * scale, BufferedImage.TYPE_INT_ARGB)
                            val g = composite.createGraphics()
                            for (dx in 0 until scale) {
                                for (dy in 0 until scale) {
                                    val (shiftX, shiftY) = when (zoom) {
                                        6 -> -8 to -2
                                        7 -> 0 to 0
                                        else -> 0 to 0
                                    }
                                    val sx = tx * scale + dx + shiftX
                                    val sy = ty * scale + (scale - dy) + shiftY
                                    val tile = sourceTileMap[sx to sy]?.let { ImageIO.read(it) }
                                    if (tile != null) {
                                        g.drawImage(tile, dx * 256, dy * 256, null)
                                    }
                                }
                            }
                            g.dispose()

                            val scaled = multiStepScaleDown(composite, 256, 256)
                            val (offsetX, offsetY) = when (zoom) {
                                7 -> 8 to 9
                                6 -> 6 to 5
                                else -> 0 to 0
                            }
                            val target = File(outputDir, "$zoom/${tx - offsetX}/${ty - offsetY}.png")
                            if (!LIVE || target.exists()) {
                                if (!LIVE) {
                                    target.parentFile.mkdirs()
                                }
                                ImageIO.write(scaled, "png", target)
                            } else {
                                println("Missing file $target")
                            }
                        }
                    }
                }.awaitAll()
            }

            println("All tiles generated in parallel from zoom $minZoom to $maxZoom.")
        }
    }
}
