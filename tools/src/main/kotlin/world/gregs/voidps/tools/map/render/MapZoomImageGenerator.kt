package world.gregs.voidps.tools.map.render

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index.WORLD_MAP
import world.gregs.voidps.cache.config.data.WorldMapInfoDefinition
import world.gregs.voidps.cache.config.decoder.WorldMapInfoDecoder
import world.gregs.voidps.cache.definition.decoder.WorldMapDetailsDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.type.Tile
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.exists

/**
 * Generates world map pngs with varying zoom levels as a ZXY tile set; used for leaflet js display in explv's map viewer
 */
object MapZoomImageGenerator {

    private const val TILE_SIZE = 256
    private const val INPUT_ZOOM = 8
    private const val OUTPUT_BASE = "./images/final/"
    private const val MAX_ZOOM_IN = 11
    private const val MIN_ZOOM_IN = 4

    /**
     * Whether to write the zoom-in levels above [INPUT_ZOOM] (see [generateZoomInLevel]).
     *
     * They're nearest-neighbour doublings of [INPUT_ZOOM], so they hold no detail it doesn't
     * already have — for the Void map that's 96,485 files and ~252 MiB reproducing the 1,477 files
     * and 8.6 MiB of level 8. The web map's `worldmap.js` no longer reads them: its `nativeZoomFor`
     * caps the level it fetches at [INPUT_ZOOM] and lets `image-rendering: pixelated` redo the
     * doubling in the browser, which draws the identical image from 1/64th of the bytes at zoom 11.
     *
     * Left as a toggle rather than deleted because the tile set is also consumed by leaflet-based
     * viewers (see the class doc), and leaflet fetches a real tile per integer zoom instead of
     * scaling one — turn this back on when generating for one of those.
     */
    private const val GENERATE_ZOOM_IN = false

    @JvmStatic
    fun main(args: Array<String>) {
        if (!Path.of("$OUTPUT_BASE/0/8/").exists()) {
            WorldMapDumper.dump(OUTPUT_BASE) { image, level, content ->
                val file = File("$OUTPUT_BASE/$level/8/${content.x}/${content.y}.png")
                file.parentFile.mkdirs()
                ImageIO.write(image, "png", file)
            }
            println("Map generation completed.")
        }
        for (level in 0 until 4) {
            println("Generating tiles for level $level")
            for (zoom in INPUT_ZOOM - 1 downTo MIN_ZOOM_IN) {
                val tiles = generateZoomOutLevel(level, zoom)
                println("Generated zoom-out level $zoom with ${tiles.size} tiles.")
            }
            if (GENERATE_ZOOM_IN) {
                for (zoom in INPUT_ZOOM + 1..MAX_ZOOM_IN) {
                    val tiles = generateZoomInLevel(level, zoom)
                    println("Generated zoom-in level $zoom with ${tiles.size} tiles.")
                }
            }
        }
        dumpLocations()
    }

    private fun generateZoomInLevel(level: Int, zoom: Int): List<Pair<Int, Int>> {
        val currentPath = "$OUTPUT_BASE/$level/$zoom"
        val parentZoom = zoom - 1
        val parentPath = "$OUTPUT_BASE/$level/$parentZoom"
        val parentTiles = parentTiles(parentPath)
        val tileSet = mutableListOf<Pair<Int, Int>>()
        runBlocking {
            val semaphore = Semaphore(16)
            for ((x, y) in parentTiles) {
                val parentFile = File("$parentPath/$x/$y.png")
                if (!parentFile.exists()) continue
                launch(Dispatchers.IO) {
                    semaphore.withPermit {
                        val parentImage = ImageIO.read(parentFile)
                        for (dx in 0..1) {
                            for (dy in 0..1) {
                                val subImage = parentImage.getSubimage(
                                    dx * TILE_SIZE / 2,
                                    dy * TILE_SIZE / 2,
                                    TILE_SIZE / 2,
                                    TILE_SIZE / 2,
                                )

                                val zoomedTile = BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB)
                                val g = zoomedTile.createGraphics()
                                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
                                g.drawImage(subImage, 0, 0, TILE_SIZE, TILE_SIZE, null)
                                g.dispose()

                                if (isImageTransparentOrBlack(zoomedTile)) {
                                    continue
                                }

                                val childX = x * 2 + dx
                                val childY = y * 2 + dy
                                val writeY = childY - dy * 2
                                val outDir = File("$currentPath/$childX")
                                outDir.mkdirs()
                                ImageIO.write(zoomedTile, "png", File(outDir, "$writeY.png"))
                                tileSet.add(Pair(childX, childY))
                            }
                        }
                    }
                }
            }
        }

        return tileSet
    }

    private fun addTopEdgeTiles(tileSet: MutableSet<Pair<Int, Int>>) {
        // For each tile, check if there's a tile directly above it
        // If not, add the missing tile above
        val tilesToAdd = mutableSetOf<Pair<Int, Int>>()

        for ((x, y) in tileSet) {
            val tileAbove = Pair(x, y + 1)
            // If there's no tile above this one, we need to add it
            if (!tileSet.contains(tileAbove)) {
                tilesToAdd.add(tileAbove)
            }
        }

        // Add all the missing tiles above
        tileSet.addAll(tilesToAdd)
    }

    private fun generateZoomOutLevel(level: Int, zoom: Int): List<Pair<Int, Int>> {
        val nextZoom = zoom + 1
        val nextZoomPath = "$OUTPUT_BASE/$level/$nextZoom"
        val currentZoomPath = "$OUTPUT_BASE/$level/$zoom"

        val nextTiles = parentTiles(nextZoomPath)

        val tileSet = mutableSetOf<Pair<Int, Int>>()

        for ((x, y) in nextTiles) {
            val parentX = x / 2
            val parentY = y / 2
            tileSet.add(Pair(parentX, parentY))
        }
        addTopEdgeTiles(tileSet)

        File(currentZoomPath).mkdirs()

        runBlocking {
            val semaphore = Semaphore(16)
            for ((x, y) in tileSet) {
                launch(Dispatchers.IO) {
                    semaphore.withPermit {
                        val img = BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB)

                        for (dx in 0..1) {
                            for (dy in 0..1) {
                                val childX = x * 2 + dx
                                val readY = if (dy == 0) y * 2 else y * 2 - 1
                                val childFile = File("$nextZoomPath/$childX/$readY.png")
                                if (childFile.exists()) {
                                    val tile = ImageIO.read(childFile)
                                    halve(tile, img, dx * TILE_SIZE / 2, dy * TILE_SIZE / 2)
                                }
                            }
                        }

                        if (isImageTransparentOrBlack(img)) {
                            return@withPermit
                        }

                        val outDir = File("$currentZoomPath/$x")
                        outDir.mkdirs()
                        ImageIO.write(img, "png", File(outDir, "$y.png"))
                    }
                }
            }
        }

        return tileSet.toList()
    }

    /**
     * Averages each 2x2 block of [source] into one pixel of [target] at [offsetX]/[offsetY] — the
     * exact area filter for a 2:1 reduction, and the reason this isn't a [java.awt.Graphics2D]
     * `drawImage` with an interpolation hint like [generateZoomInLevel]'s. Java2D's BICUBIC (and
     * BILINEAR) sample a neighbourhood around a point instead of pre-filtering to the target scale,
     * so halving pixel art through either softens every edge and rings around it: measured against
     * the Lumbridge tile, bicubic zoom-7 output holds 4148 distinct colours against the 297 of the
     * zoom-8 tile it came from, nearly all of them overshoot either side of an edge. Every level
     * below [INPUT_ZOOM] is generated from the one above, so that softening compounds the further
     * out you zoom — which is what made the web map's own zoom go soft below 7.5, the point where
     * it stops scaling zoom-8 tiles and starts using zoom 7.
     *
     * Colours are weighted by alpha (and unweighted again on the way out) so a block straddling the
     * transparent edge of the map averages the colours actually there, rather than dragging them
     * toward the arbitrary RGB stored under a fully transparent pixel.
     */
    private fun halve(source: BufferedImage, target: BufferedImage, offsetX: Int, offsetY: Int) {
        val half = TILE_SIZE / 2
        val pixels = source.getRGB(0, 0, TILE_SIZE, TILE_SIZE, null, 0, TILE_SIZE)
        val out = IntArray(half * half)
        for (y in 0 until half) {
            for (x in 0 until half) {
                var alpha = 0
                var red = 0
                var green = 0
                var blue = 0
                for (dy in 0..1) {
                    for (dx in 0..1) {
                        val argb = pixels[(y * 2 + dy) * TILE_SIZE + x * 2 + dx]
                        val a = argb ushr 24 and 0xff
                        alpha += a
                        red += (argb shr 16 and 0xff) * a
                        green += (argb shr 8 and 0xff) * a
                        blue += (argb and 0xff) * a
                    }
                }
                if (alpha == 0) {
                    continue
                }
                out[y * half + x] = (alpha / 4 shl 24) or (red / alpha shl 16) or (green / alpha shl 8) or (blue / alpha)
            }
        }
        target.setRGB(offsetX, offsetY, half, half, out, 0, half)
    }

    private fun parentTiles(parentPath: String): List<Pair<Int, Int>> {
        return File(parentPath).walkTopDown()
            .filter { it.isFile && it.extension == "png" }
            .mapNotNull {
                val parts = it.relativeTo(File(parentPath)).path.removeSuffix(".png").split(File.separator)
                if (parts.size != 2) return@mapNotNull null
                val x = parts[0].toIntOrNull()
                val y = parts[1].toIntOrNull()
                if (x != null && y != null) Pair(x, y) else null
            }.toList()
    }

    private fun dumpLocations() {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val worldMapInfoDefinitions = WorldMapInfoDecoder().load(cache)
        val worldMapDefinitions = WorldMapDetailsDecoder().load(cache)
        val list = mutableListOf<Pair<Tile, WorldMapInfoDefinition>>()
        for (worldMapDefinition in worldMapDefinitions) {
            val stringId = "${worldMapDefinition.map}_staticelements"
            val archiveId = cache.archiveId(WORLD_MAP, stringId)
            var length = cache.fileCount(WORLD_MAP, archiveId)
            if (length <= 0) {
                continue
            }
            val positions = IntArray(length)
            val ids = IntArray(length)
            val aBoolean1313 = false
            var counter = 0
            var index = 0
            while (length > counter) {
                val file = cache.data(WORLD_MAP, archiveId, index++) ?: continue
                val buffer = ArrayReader(file)
                val position = buffer.readInt()
                val id = buffer.readShort()
                val skip = buffer.readUnsignedByte()
                if (skip == 1) {
                    val x = position shr 14 and 0x3fff
                    val y = position and 0x3fff
                    val level = position shr 28 and 0x3
//                println("Skip $x $y $level")
                }
                if (aBoolean1313 && skip == 1) {
                    length--
                } else {
                    positions[counter] = position
                    ids[counter] = id
                    counter++
                }
            }
            fun loadAreaNames(length: Int, positions: IntArray, ids: IntArray) {
                val sections = worldMapDefinition.sections ?: return
                for (i in 0 until length) {
                    val x = positions[i] shr 14 and 0x3fff
                    val y = positions[i] and 0x3fff
                    val level = positions[i] shr 28 and 0x3
                    for (it in sections) {
                        if (level == it.level && it.minX <= x && x <= it.maxX && y >= it.minY && y <= it.maxY) {
                            val tile = Tile(x, y, level)
                            val def = worldMapInfoDefinitions[ids[i]]
                            if (def.spriteId != -1 || def.name.isNullOrBlank() || def.hiddenOnWorldMap) {
                                continue
                            }
                            list.add(tile to worldMapInfoDefinitions[ids[i]])
                            break
                        }
                    }
                }
            }
            loadAreaNames(length, positions, ids)
        }

        val builder = StringBuilder()
        builder.appendLine("{")
        builder.appendLine("    \"locations\": [")
        for ((tile, def) in list.sortedBy { it.second.name }) {
            builder.appendLine(
                """        { "name": "${def.name?.replace("<br>", " ")}", "coords": [${tile.x}, ${tile.y}, ${tile.level}], "size": "${
                    when (def.fontSize) {
                        2 -> "large"
                        1 -> "medium"
                        else -> "default"
                    }
                }" },""",
            )
        }
        File("${OUTPUT_BASE}/locations.json").writeText("${builder.dropLast(2)}\n    ]\n}")
    }

    private fun isImageTransparentOrBlack(img: BufferedImage): Boolean {
        val width = img.width
        val height = img.height
        val raster = img.raster
        val hasAlpha = img.colorModel.hasAlpha()

        val pixels = IntArray(4) // RGBA or RGB

        for (y in 0 until height) {
            for (x in 0 until width) {
                raster.getPixel(x, y, pixels)

                val r = pixels[0]
                val g = pixels[1]
                val b = pixels[2]
                val a = if (hasAlpha) pixels.getOrElse(3) { 255 } else 255

                // If any pixel is not fully transparent and not fully black, it's not ignorable
                if (a != 0 && (r != 0 || g != 0 || b != 0)) {
                    return false
                }
            }
        }
        return true
    }
}
