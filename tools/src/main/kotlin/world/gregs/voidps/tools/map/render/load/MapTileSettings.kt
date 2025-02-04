package world.gregs.voidps.tools.map.render.load

import world.gregs.voidps.cache.config.data.OverlayDefinition
import world.gregs.voidps.cache.config.data.UnderlayDefinition
import world.gregs.voidps.cache.definition.data.MapTile
import world.gregs.voidps.cache.definition.data.MaterialDefinition
import world.gregs.voidps.tools.map.render.draw.TileLevel
import world.gregs.voidps.tools.map.render.load.MapConstants.GROUND_BLENDING
import world.gregs.voidps.tools.map.render.load.MapConstants.TILE_TYPE_HEIGHT_OVERRIDE
import world.gregs.voidps.tools.map.render.load.MapConstants.WATER_MOVEMENT
import world.gregs.voidps.tools.map.render.load.MapConstants.firstTileTypeVertices
import world.gregs.voidps.tools.map.render.load.MapConstants.overlaySizes
import world.gregs.voidps.tools.map.render.load.MapConstants.secondTileTypeVertices
import world.gregs.voidps.tools.map.render.load.MapConstants.thirdTileTypeVertices
import world.gregs.voidps.tools.map.render.load.MapConstants.tileXOffsets
import world.gregs.voidps.tools.map.render.load.MapConstants.tileYOffsets
import world.gregs.voidps.tools.map.render.load.MapConstants.underlaySizes
import world.gregs.voidps.tools.map.render.raster.ColourPalette
import world.gregs.voidps.type.Region

class MapTileSettings(
    private val levelCount: Int,
    private val underlayDecoder: Array<UnderlayDefinition>,
    private val overlayDecoder: Array<OverlayDefinition>,
    private val textureDecoder: Array<MaterialDefinition>,
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

    fun tile(level: Int, localX: Int, localY: Int): MapTile {
        val regionX = this.regionX + (localX / 64)
        val regionY = this.regionY + (localY / 64)
        val regionId = Region.id(regionX, regionY)
        return manager.tiles[regionId]?.getTile(localX.rem(64), localY.rem(64), level) ?: MapTile.EMPTY
    }

    fun set(regionX: Int, regionY: Int) {
        this.regionX = regionX
        this.regionY = regionY
    }

    private val level = (0 until levelCount).map { level ->
        TileLevel(textureDecoder, width, height, level, manager.tiles)
    }

    fun load(): List<TileLevel> {
        loadSettings()
        loadUnderlays(null)
        return level
    }

    private fun loadSettings() = level.forEach { level ->
        var flag = 0
        var settings = 0
        if (!WATER_MOVEMENT) {
            if (MapConstants.TILE_WATER) {
                settings = settings or 0x8
            }
            if (MapConstants.TILE_LIGHTING) {
                flag = flag or 0x2
            }
            if (MapConstants.SCENERY_SHADOWS != 0) {
                flag = flag or 0x1
                if ((level.level == 0) or MapConstants.A_BOOLEAN_8715) {
                    settings = settings or 0x10
                }
            }
        }
        if (MapConstants.TILE_LIGHTING) {
            settings = settings or 0x7
        }
        if (!MapConstants.A_BOOLEAN_10563) {
            settings = settings or 0x20
        }
        level.loadBrightness()
        level.settings = settings
    }

    private fun loadUnderlays(tileLevel: TileLevel?) {
        val colours = Array(width) { IntArray(height) }
        for (level in 0 until levelCount) {
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
                        val underlay = tile(level, maxX, y).underlayId
                        val underlayDefinition = underlayDecoder.getOrNull(underlay - 1)
                        if (underlayDefinition != null) {
                            underlayHue[y] += underlayDefinition.hue
                            underlaySaturation[y] += underlayDefinition.saturation
                            underlayLightness[y] += underlayDefinition.lightness
                            underlayChroma[y] += underlayDefinition.chroma
                            underlayChangeCount[y]++
                        }
                    }
                    val minX = dx - samplingX
                    if (minX >= 0) {
                        val underlay = tile(level, minX, y).underlayId
                        val underlayDefinition = underlayDecoder.getOrNull(underlay - 1)
                        if (underlayDefinition != null) {
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
            loadTileVertices(level, colours, if (level == 0) tileLevel else null, this.level[level])
        }
    }

    private fun loadTileVertices(level: Int, parentColours: Array<IntArray>, aboveLevel: TileLevel?, tileLevel: TileLevel) {
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (GROUND_BLENDING == -1 || useUnderlay(x, y, GROUND_BLENDING, level)) {
                    val tile = tile(level, x, y)
                    var tileType = tile.overlayPath
                    val tileDirection = tile.overlayRotation
                    val overlay = tile.overlayId
                    val underlay = tile.underlayId
                    var overlayDefinition = if (overlay != 0) overlayDecoder.getOrNull(overlay - 1) else null
                    if (tileType == 0 && overlayDefinition == null) {
                        tileType = 12
                    }
                    val underlayDefinition = if (underlay != 0) underlayDecoder.getOrNull(underlay - 1) else null
                    if (overlayDefinition != null && overlayDefinition.colour == -1 && overlayDefinition.blendColour == -1) {
                        overlayDefinition = null
                    }
                    if (overlayDefinition != null || underlayDefinition != null) {
                        val underlaySize = underlaySizes[tileType]
                        val overlaySize = overlaySizes[tileType]
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
                            repeat(overlaySize) {
                                vertexIndices1[xIndex] = firstTileTypeVertices[tileType][yIndex]
                                vertexIndices2[xIndex] = secondTileTypeVertices[tileType][yIndex]
                                vertexIndices3[xIndex] = thirdTileTypeVertices[tileType][yIndex]
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
                            repeat(underlaySize) {
                                vertexIndices1[xIndex] = firstTileTypeVertices[tileType][yIndex]
                                vertexIndices2[xIndex] = secondTileTypeVertices[tileType][yIndex]
                                vertexIndices3[xIndex] = thirdTileTypeVertices[tileType][yIndex]
                                textures[xIndex] = underlayTexture
                                scales[xIndex] = underlayDefinition.scale
                                colours[xIndex] = parentColours[x][y]
                                if (blendColours != null) {
                                    blendColours[xIndex] = colours[xIndex]
                                }
                                yIndex++
                                xIndex++
                            }
                        }
                        val offsetSize = tileXOffsets.size
                        val xOffsets = IntArray(offsetSize)
                        val yOffsets = IntArray(offsetSize)
                        val heightOffsets = if (aboveLevel == null) null else IntArray(offsetSize)
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
                            if (heightOffsets != null && TILE_TYPE_HEIGHT_OVERRIDE[tileType][index]) {
                                val dx = xOffsets[index] + (x shl 9)
                                val dy = (y shl 9) + yOffsets[index]
                                heightOffsets[index] = aboveLevel!!.averageHeight(dy, dx) - tileLevel.averageHeight(dy, dx)
                            }
                        }
                        tileLevel.calculateColours(x, y, xOffsets, heightOffsets, yOffsets, vertexIndices1, vertexIndices2, vertexIndices3, colours, blendColours, textures, scales)
                    }
                }
            }
        }
    }

    fun useUnderlay(x: Int, y: Int, currentLevel: Int, otherLevel: Int): Boolean {
        if (tile(0, x, y).settings and BRIDGE_TILE != 0) {
            return true
        }
        return if (tile(otherLevel, x, y).settings and 0x10 != 0) false else currentLevel == offsetLevel(y, x, otherLevel)
    }

    private fun offsetLevel(y: Int, x: Int, level: Int): Int {
        if (tile(level, x, y).settings and 0x8 != 0) {
            return 0
        }
        return if (level > 0 && tile(1, x, y).settings and BRIDGE_TILE != 0) level - 1 else level
    }

    companion object {
        private const val BRIDGE_TILE = 0x2
    }
}
