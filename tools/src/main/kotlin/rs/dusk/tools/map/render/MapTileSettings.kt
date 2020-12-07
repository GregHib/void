package rs.dusk.tools.map.render

import rs.dusk.cache.config.decoder.OverlayDecoder
import rs.dusk.cache.config.decoder.UnderlayDecoder
import rs.dusk.cache.definition.decoder.TextureDecoder
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.tile.TileData
import rs.dusk.tools.map.render.Plane.Companion.emptyTile
import rs.dusk.tools.map.render.Render.TILE_TYPE_HEIGHT_OVERRIDE
import rs.dusk.tools.map.render.Render.firstTileTypeVertices
import rs.dusk.tools.map.render.Render.groundBlending
import rs.dusk.tools.map.render.Render.overlaySizes
import rs.dusk.tools.map.render.Render.secondTileTypeVertices
import rs.dusk.tools.map.render.Render.thirdTileTypeVertices
import rs.dusk.tools.map.render.Render.tileXOffsets
import rs.dusk.tools.map.render.Render.tileYOffsets
import rs.dusk.tools.map.render.Render.underlaySizes
import rs.dusk.tools.map.render.Render.waterMovement

class MapTileSettings(
    private val width: Int,
    private val height: Int,
    private val planeCount: Int,
    private val underlayDecoder: UnderlayDecoder,
    private val overlayDecoder: OverlayDecoder,
    private val textureDecoder: TextureDecoder,
    private val samplingX: Int = 5,
    private val samplingY: Int = 5,
    private val tiles: Map<Int, Array<Array<Array<TileData?>>>>,
    private val regionX: Int = 0,
    private val regionY: Int = 0
) {

    var underlayLightness = IntArray(height)
    var underlayChangeCount = IntArray(height)
    var underlayHue = IntArray(height)
    var underlayChroma = IntArray(height)
    var underlaySaturation = IntArray(height)

    fun tile(plane: Int, localX: Int, localY: Int): TileData {
        val regionX = this.regionX + (localX / 64)
        val regionY = this.regionY + (localY / 64)
        val regionId = Region.getId(regionX, regionY)
        return tiles[regionId]?.get(plane)?.get(localX.rem(64))?.get(localY.rem(64)) ?: emptyTile
    }

    fun load(): List<JavaPlane> {
        val planes = loadSettings()
        loadUnderlays(null, planes)
        return planes
    }

    fun loadSettings() = (0 until planeCount).map { plane ->
        var i_23_ = 0
        var settings = 0
        if (!waterMovement) {
            if (Render.tileWater) {
                settings = settings or 0x8
            }
            if (Render.tileLighting) {
                i_23_ = i_23_ or 0x2
            }
            if (Render.sceneryShadows != 0) {
                i_23_ = i_23_ or 0x1
                if ((plane == 0) or Render.aBoolean8715) {
                    settings = settings or 0x10
                }
            }
        }
        if (Render.tileLighting) {
            settings = settings or 0x7
        }
        if (!Render.aBoolean10563) {
            settings = settings or 0x20
        }
        JavaPlane(textureDecoder, settings, width, height, plane, tiles)
    }

    fun loadUnderlays(tilePlane: Plane?, planeList: List<JavaPlane>) {
        if (height != underlayHue.size) {
            underlayLightness = IntArray(height)
            underlayChangeCount = IntArray(height)
            underlayHue = IntArray(height)
            underlayChroma = IntArray(height)
            underlaySaturation = IntArray(height)
        }

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
                            colours[dx][dy] = Render.hslToPaletteIndex(lightness / total, saturation / total, hue * 256 / chroma)
                        }
                    }
                }
            }
            loadTileVertices(plane, colours, if (plane == 0) tilePlane else null, planeList[plane])
        }
    }

    private fun loadTileVertices(plane: Int, parentColours: Array<IntArray>, abovePlane: Plane?, tilePlane: JavaPlane) {
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
                            when {
                                tileDirection == 0 -> {
                                    xOffsets[index] = offsetX
                                    yOffsets[index] = offsetY
                                }
                                tileDirection == 1 -> {
                                    xOffsets[index] = offsetY
                                    yOffsets[index] = -offsetX + 512
                                }
                                tileDirection == 2 -> {
                                    xOffsets[index] = 512 - offsetX
                                    yOffsets[index] = 512 - offsetY
                                }
                                tileDirection == 3 -> {
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

    fun offsetPlane(y: Int, x: Int, plane: Int): Int {
        if (tile(plane, x, y).settings.toInt() and 0x8 != 0) {
            return 0
        }
        return if (plane > 0 && tile(1, x, y).settings.toInt() and BRIDGE_TILE != 0) plane - 1 else plane
    }

    companion object {
        private val BRIDGE_TILE = 0x2
    }
}
