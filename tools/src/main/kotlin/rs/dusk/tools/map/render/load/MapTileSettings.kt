package rs.dusk.tools.map.render.load

import rs.dusk.cache.config.decoder.OverlayDecoder
import rs.dusk.cache.config.decoder.UnderlayDecoder
import rs.dusk.cache.definition.decoder.TextureDecoder
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.tile.TileData
import rs.dusk.tools.map.render.draw.TilePlane
import rs.dusk.tools.map.render.draw.TilePlane.Companion.emptyTile
import rs.dusk.tools.map.render.load.MapConstants.TILE_TYPE_HEIGHT_OVERRIDE
import rs.dusk.tools.map.render.load.MapConstants.firstTileTypeVertices
import rs.dusk.tools.map.render.load.MapConstants.groundBlending
import rs.dusk.tools.map.render.load.MapConstants.overlaySizes
import rs.dusk.tools.map.render.load.MapConstants.secondTileTypeVertices
import rs.dusk.tools.map.render.load.MapConstants.thirdTileTypeVertices
import rs.dusk.tools.map.render.load.MapConstants.tileXOffsets
import rs.dusk.tools.map.render.load.MapConstants.tileYOffsets
import rs.dusk.tools.map.render.load.MapConstants.underlaySizes
import rs.dusk.tools.map.render.load.MapConstants.waterMovement
import rs.dusk.tools.map.render.raster.ColourPalette

class MapTileSettings(
    private val planeCount: Int,
    private val underlayDecoder: UnderlayDecoder,
    private val overlayDecoder: OverlayDecoder,
    private val textureDecoder: TextureDecoder,
    private val samplingX: Int = 5,
    private val samplingY: Int = 5,
    private val manager: RegionManager
) {
    private val width: Int = manager.width
    private val height: Int = manager.height

    private val underlayLightness = IntArray(height)
    private val underlayChangeCount = IntArray(height)
    private val underlayHue = IntArray(height)
    private val underlayChroma = IntArray(height)
    private val underlaySaturation = IntArray(height)

    private var regionX: Int = 0
    private var regionY: Int = 0

    fun tile(plane: Int, localX: Int, localY: Int): TileData {
        val regionX = this.regionX + (localX / 64)
        val regionY = this.regionY + (localY / 64)
        val regionId = Region.getId(regionX, regionY)
        return manager.tiles[regionId]?.get(plane)?.get(localX.rem(64))?.get(localY.rem(64)) ?: emptyTile
    }

    fun set(regionX: Int, regionY: Int) {
        this.regionX = regionX
        this.regionY = regionY
    }

    private val planes = (0 until planeCount).map { plane ->
        TilePlane(textureDecoder, width, height, plane, manager.tiles)
    }

    fun load(): List<TilePlane> {
        loadSettings()
        loadUnderlays(null)
        return planes
    }

    fun loadSettings() = planes.forEach { plane ->
        var i_23_ = 0
        var settings = 0
        if (!waterMovement) {
            if (MapConstants.tileWater) {
                settings = settings or 0x8
            }
            if (MapConstants.tileLighting) {
                i_23_ = i_23_ or 0x2
            }
            if (MapConstants.sceneryShadows != 0) {
                i_23_ = i_23_ or 0x1
                if ((plane.plane == 0) or MapConstants.aBoolean8715) {
                    settings = settings or 0x10
                }
            }
        }
        if (MapConstants.tileLighting) {
            settings = settings or 0x7
        }
        if (!MapConstants.aBoolean10563) {
            settings = settings or 0x20
        }
        plane.loadBrightness()
        plane.settings = settings
    }

    fun loadUnderlays(tilePlane: TilePlane?) {
        val colours = Array(width) { IntArray(height) }
        for (plane in 0 until planeCount) {
            for (y in 0 until height) {
                underlayHue[y] = 0
                underlaySaturation[y] = 0
                underlayLightness[y] = 0
                underlayChroma[y] = 0
                underlayChangeCount[y] = 0
            }
            for (dx in -samplingX until width) {
                for (y in 0 until height) {
                    val maxX = dx + samplingX
                    if (maxX < width) {
                        val underlay = tile(plane, maxX, y).underlayId
                        if (underlay > 0) {
                            val underlayDefinition = underlayDecoder.get(underlay - 1)
                            underlayHue[y] += underlayDefinition.hue
                            underlaySaturation[y] += underlayDefinition.saturation
                            underlayLightness[y] += underlayDefinition.lightness
                            underlayChroma[y] += underlayDefinition.chroma
                            underlayChangeCount[y]++
                        }
                    }
                    val minX = dx - samplingX
                    if (minX >= 0) {
                        val underlay = tile(plane, minX, y).underlayId
                        if (underlay > 0) {
                            val underlayDefinition = underlayDecoder.get(underlay - 1)
                            underlayHue[y] -= underlayDefinition.hue
                            underlaySaturation[y] -= underlayDefinition.saturation
                            underlayLightness[y] -= underlayDefinition.lightness
                            underlayChroma[y] -= underlayDefinition.chroma
                            underlayChangeCount[y]--
                        }
                    }
                }
                if (dx >= 0) {
                    var hue = 0
                    var saturation = 0
                    var lightness = 0
                    var chroma = 0
                    var total = 0
                    for (dy in -samplingY until height) {
                        val maxY = samplingY + dy
                        if (maxY < height) {
                            saturation += underlaySaturation[maxY]
                            hue += underlayHue[maxY]
                            chroma += underlayChroma[maxY]
                            lightness += underlayLightness[maxY]
                            total += underlayChangeCount[maxY]
                        }
                        val minY = dy - samplingY
                        if (minY >= 0) {
                            lightness -= underlayLightness[minY]
                            chroma -= underlayChroma[minY]
                            hue -= underlayHue[minY]
                            total -= underlayChangeCount[minY]
                            saturation -= underlaySaturation[minY]
                        }
                        if (dy >= 0 && chroma > 0 && total > 0) {
                            colours[dx][dy] = ColourPalette.hslToPaletteIndex(lightness / total, saturation / total, hue * 256 / chroma)
                        }
                    }
                }
            }
            loadTileVertices(plane, colours, if (plane == 0) tilePlane else null, planes[plane])
        }
    }

    private fun loadTileVertices(plane: Int, parentColours: Array<IntArray>, abovePlane: TilePlane?, tilePlane: TilePlane) {
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (groundBlending == -1 || useUnderlay(x, y, groundBlending, plane)) {
                    val tile = tile(plane, x, y)
                    var tileType = tile.overlayPath.toByte()
                    val tileDirection = tile.overlayRotation
                    val overlay = tile.overlayId and 0xff
                    val underlay = tile.underlayId and 0xff
                    var overlayDefinition = if (overlay != 0) overlayDecoder.get(overlay - 1) else null
                    if (tileType.toInt() == 0 && overlayDefinition == null) {
                        tileType = 12.toByte()
                    }
                    val underlayDefinition = if (underlay != 0) underlayDecoder.get(underlay - 1) else null
                    if (overlayDefinition != null && overlayDefinition.colour == -1 && overlayDefinition.blendColour == -1) {
                        overlayDefinition = null
                    }
                    if (overlayDefinition != null || underlayDefinition != null) {
                        val underlaySize = underlaySizes[tileType.toInt()]
                        val overlaySize = overlaySizes[tileType.toInt()]
                        val size = (if (underlayDefinition == null) 0 else underlaySize) + if (overlayDefinition != null) overlaySize else 0
                        var xIndex = 0
                        var yIndex = 0
                        val overlayTexture = overlayDefinition?.texture ?: -1
                        val underlayTexture = underlayDefinition?.texture ?: -1
                        val vertexIndices1 = IntArray(size)
                        val vertexIndices2 = IntArray(size)
                        val vertexIndices3 = IntArray(size)
                        val colours = IntArray(size)
                        val textures = IntArray(size)
                        val scales = IntArray(size)
                        val blendColours = if (overlayDefinition != null && overlayDefinition.blendColour != -1) IntArray(size) else null
                        if (overlayDefinition == null) {
                            yIndex += overlaySize
                        } else {
                            for (i_49_ in 0 until overlaySize) {
                                vertexIndices1[xIndex] = firstTileTypeVertices[tileType.toInt()][yIndex]
                                vertexIndices2[xIndex] = secondTileTypeVertices[tileType.toInt()][yIndex]
                                vertexIndices3[xIndex] = thirdTileTypeVertices[tileType.toInt()][yIndex]
                                textures[xIndex] = overlayTexture
                                scales[xIndex] = overlayDefinition.scale
                                colours[xIndex] = overlayDefinition.colour
                                if (blendColours != null) {
                                    blendColours[xIndex] = overlayDefinition.blendColour
                                }
                                xIndex++
                                yIndex++
                            }
                        }
                        if (underlayDefinition != null) {
                            var count = 0
                            while (underlaySize > count) {
                                vertexIndices1[xIndex] = firstTileTypeVertices[tileType.toInt()][yIndex]
                                vertexIndices2[xIndex] = secondTileTypeVertices[tileType.toInt()][yIndex]
                                vertexIndices3[xIndex] = thirdTileTypeVertices[tileType.toInt()][yIndex]
                                textures[xIndex] = underlayTexture
                                scales[xIndex] = underlayDefinition.scale
                                colours[xIndex] = parentColours[x][y]
                                if (blendColours != null) {
                                    blendColours[xIndex] = colours[xIndex]
                                }
                                yIndex++
                                xIndex++
                                count++
                            }
                        }
                        val offsetSize = tileXOffsets.size
                        val xOffsets = IntArray(offsetSize)
                        val yOffsets = IntArray(offsetSize)
                        val heightOffsets = if (abovePlane == null) null else IntArray(offsetSize)
                        for (index in 0 until offsetSize) {
                            val offsetX = tileXOffsets[index]
                            val offsetY = tileYOffsets[index]
                            when (tileDirection) {
                                0 -> {
                                    xOffsets[index] = offsetX
                                    yOffsets[index] = offsetY
                                }
                                1 -> {
                                    xOffsets[index] = offsetY
                                    yOffsets[index] = -offsetX + 512
                                }
                                2 -> {
                                    xOffsets[index] = 512 - offsetX
                                    yOffsets[index] = 512 - offsetY
                                }
                                3 -> {
                                    xOffsets[index] = 512 + -offsetY
                                    yOffsets[index] = offsetX
                                }
                            }
                            if (heightOffsets != null && TILE_TYPE_HEIGHT_OVERRIDE[tileType.toInt()][index]) {
                                val dx = xOffsets[index] + (x shl 9)
                                val dy = (y shl 9) + yOffsets[index]
                                heightOffsets[index] = abovePlane!!.averageHeight(dy, dx) - tilePlane.averageHeight(dy, dx)
                            }
                        }
                        tilePlane.calculateColours(x, y, xOffsets, heightOffsets, yOffsets, vertexIndices1, vertexIndices2, vertexIndices3, colours, blendColours, textures, scales)
                    }
                }
            }
        }
    }

    fun useUnderlay(x: Int, y: Int, currentPlane: Int, otherPlane: Int): Boolean {
        if (tile(0, x, y).settings.toInt() and BRIDGE_TILE != 0) {
            return true
        }
        return if (tile(otherPlane, x, y).settings.toInt() and 0x10 != 0) false else currentPlane == offsetPlane(y, x, otherPlane)
    }

    private fun offsetPlane(y: Int, x: Int, plane: Int): Int {
        if (tile(plane, x, y).settings.toInt() and 0x8 != 0) {
            return 0
        }
        return if (plane > 0 && tile(1, x, y).settings.toInt() and BRIDGE_TILE != 0) plane - 1 else plane
    }

    companion object {
        private const val BRIDGE_TILE = 0x2
    }
}
