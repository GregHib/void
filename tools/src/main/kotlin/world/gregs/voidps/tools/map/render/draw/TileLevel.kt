package world.gregs.voidps.tools.map.render.draw

import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapTile
import world.gregs.voidps.cache.definition.data.MaterialDefinition
import world.gregs.voidps.tools.map.render.load.MapConstants.SIZE
import world.gregs.voidps.tools.map.render.load.MapTileSettings
import world.gregs.voidps.tools.map.render.model.TextureColours
import world.gregs.voidps.tools.map.render.model.TileColours
import world.gregs.voidps.tools.map.render.raster.Raster
import world.gregs.voidps.type.Region
import kotlin.math.sqrt

class TileLevel(
    private val materialDefinitions: Array<MaterialDefinition>,
    private val width: Int,
    private val height: Int,
    val level: Int,
    private val tiles: Map<Int, MapDefinition>
) {

    private val tileBrightness = Array(width + 1) { ByteArray(height + 1) }
    private val tileShadows = Array(width + 1) { ByteArray(height + 1) }

    // Java toolkit stuff
    private var tileColours: Array<Array<TileColours?>>? = null
    private var textureColours: Array<Array<TextureColours?>>? = null
    private var lightX: Int = 0
    private var lightY: Int = 0
    private var lightZ: Int = 0
    var settings: Int = 0

    private fun setBrightness(brightnessX: Float, brightnessY: Float, brightnessZ: Float) {
        val total = sqrt((brightnessX * brightnessX + brightnessY * brightnessY + brightnessZ * brightnessZ).toDouble()).toFloat()
        lightX = (brightnessX * 65535.0f / total).toInt()// -36578
        lightY = (brightnessY * 65535.0f / total).toInt()// -40235
        lightZ = (brightnessZ * 65535.0f / total).toInt()// -36578
    }

    fun loadBrightness() {
        tileColours = null
        textureColours = null
        setBrightness(-200f, -220f, -200f)
        val brightness: Int = BRIGHTNESS shr 9
        for (y in 1 until height) {
            for (x in 1 until width) {
                var intensity = brightness
                val dx = tile(x + 1, y).height - tile(x - 1, y).height
                val dy = tile(x, y + 1).height - tile(x, y - 1).height
                val distance = sqrt((dx * dx + 262144 + dy * dy).toDouble()).toInt()
                val lightX = (dx shl 8) / distance
                val lightY = -262144 / distance
                val lightZ = (dy shl 8) / distance
                intensity += this.lightX * lightX + this.lightY * lightY + this.lightZ * lightZ shr 17
                intensity = intensity shr 1
                if (intensity < 2) {
                    intensity = 2
                } else if (intensity > 126) {
                    intensity = 126
                }
                tileBrightness[x][y] = intensity.toByte()
            }
        }

        // Fixed for blurred edges
        for (y in 0..height) {
            tileBrightness[0][y] = 126.toByte()
            tileBrightness[width][y] = 126.toByte()
        }
        for (x in 0..width) {
            tileBrightness[x][0] = 126.toByte()
            tileBrightness[x][height] = 126.toByte()
        }
    }

    fun tile(x: Int, y: Int): MapTile {
        val regionX = x / 64
        val regionY = y / 64
        val regionId = Region.id(regionX, regionY)
        return tiles[regionId]?.getTile(x.rem(64), y.rem(64), level) ?: MapTile.EMPTY
    }

    fun averageHeight(worldY: Int, worldX: Int): Int {
        val x = worldX shr tileScale
        val y = worldY shr tileScale
        if (x < 0 || y < 0 || x > width + -1 || -1 + height < y) {
            return 0
        }
        val dx: Int = tileUnits - 1 and worldX
        val dy: Int = tileUnits - 1 and worldY
        val a = dx * tile(1 + x, y).height + (tileUnits - dx) * tile(x, y).height shr tileScale
        val b = tile(x, y + 1).height * (tileUnits + -dx) + dx * tile(x + 1, y + 1).height shr tileScale
        return (-dy + tileUnits) * a + dy * b shr tileScale
    }

    private fun hslToHsv(hsl: Int): Short {
        val hue = hsl shr 10 and 0x3f
        var saturation = hsl shr 3 and 0x70
        val light = hsl and 0x7f
        saturation = if (light <= 64) light * saturation shr 7 else saturation * (-light + 127) shr 7
        val value = saturation + light
        val sat = if (value == 0) {
            saturation shl 1
        } else {
            (saturation shl 8) / value
        }
        return (sat shr 4 shl 7 or (hue shl 10) or value).toShort()
    }

    private fun isTypeFourEightNine(i: Int): Boolean {
        if (settings and 0x8 == 0) {// not water
            return false
        }
        if (i == 4) {
            return true
        }
        return if (i == 8) {
            true
        } else i == 9
    }

    private fun textureColour(i: Int): Int {
        return materialDefinitions[i].colour and 0xffff
    }

    private fun light(light: Int, colour: Int): Int {
        var lighting = light
        lighting = (colour and 0x7f) * lighting shr 7
        if (lighting >= 2) {
            if (lighting > 126) {
                lighting = 126
            }
        } else {
            lighting = 2
        }
        return lighting + (colour and 0xff80)
    }

    fun drawTiles(startX: Int, startY: Int, endX: Int, endY: Int, currentLevel: Int, useUnderlay: MapTileSettings, raster: Raster) {
        val actualWidth = (endY - startY) * 1024 / 256// width * 4
        var y = 0
        var x = actualWidth
        var xOffsets: IntArray? = null
        var yOffsets: IntArray? = null
        for (localX in startX until endX) {
            for (localY in startY until endY) {
                if (useUnderlay.useUnderlay(localX - startX, localY - startY, currentLevel, level)) {
                    if (tileColours?.get(localX)?.get(localY) != null) {
                        val colours = tileColours!![localX][localY]!!
                        if (colours.textureId.toInt() != -1 && colours.type.toInt() and 0x2 == 0 && colours.initialColourIndex == -1) {
                            val texture = textureColour(colours.textureId.toInt())
                            // water
                            raster.drawGouraudTriangle(x - SIZE, x - SIZE, x, y + SIZE, y, y + SIZE, light(colours.northEastColourIndex.toInt() and 0xffff, texture), light(colours.northColourIndex.toInt() and 0xffff, texture), light(colours.eastColourIndex.toInt() and 0xffff, texture))
                            raster.drawGouraudTriangle(x, x, x - SIZE, y, y + SIZE, y, light(colours.middleColourIndex.toInt() and 0xffff, texture), light(colours.eastColourIndex.toInt() and 0xffff, texture), light(colours.northColourIndex.toInt() and 0xffff, texture))
                        } else if (colours.initialColourIndex == -1) {
                            // normal tiles
                            raster.drawGouraudTriangle(x - SIZE, x - SIZE, x, y + SIZE, y, y + SIZE, colours.northEastColourIndex.toInt() and 0xffff, colours.northColourIndex.toInt() and 0xffff, colours.eastColourIndex.toInt() and 0xffff)
                            raster.drawGouraudTriangle(x, x, x - SIZE, y, y + SIZE, y, colours.middleColourIndex.toInt() and 0xffff, colours.eastColourIndex.toInt() and 0xffff, colours.northColourIndex.toInt() and 0xffff)
                        } else {
                            // overlay
                            val brightness = colours.initialColourIndex
                            raster.drawGouraudTriangle(x - SIZE, x - SIZE, x, y + SIZE, y, y + SIZE, brightness, brightness, brightness)
                            raster.drawGouraudTriangle(x, x, x - SIZE, y, y + SIZE, y, brightness, brightness, brightness)
                        }
                    } else if (textureColours?.get(localX)?.get(localY) != null) {
                        val colours = textureColours!![localX][localY]!!
                        if (xOffsets == null || yOffsets == null) {
                            xOffsets = IntArray(width)
                            yOffsets = IntArray(height)
                        }
                        for (index in 0 until colours.size) {
                            xOffsets[index] = y + colours.xOffsets!![index] * SIZE / tileUnits
                            yOffsets[index] = x - colours.yOffsets!![index] * SIZE / tileUnits
                        }
                        for (index in 0 until colours.count) {
                            val i1 = colours.vertexIndices1!![index].toInt()
                            val i2 = colours.vertexIndices2!![index].toInt()
                            val i3 = colours.vertexIndices3!![index].toInt()
                            val xOffset1 = xOffsets[i1]
                            val xOffset2 = xOffsets[i2]
                            val xOffset3 = xOffsets[i3]
                            val yOffset1 = yOffsets[i1]
                            val yOffset2 = yOffsets[i2]
                            val yOffset3 = yOffsets[i3]
                            val brightness = colours.brightness!!
                            if (colours.hsvBlendColours != null && colours.hsvBlendColours!![index] != -1) {
                                // overlay corners
                                val blend = colours.hsvBlendColours!![index]
                                raster.drawGouraudTriangle(
                                    yOffset1,
                                    yOffset2,
                                    yOffset3,
                                    xOffset1,
                                    xOffset2,
                                    xOffset3,
                                    light(brightness[i1].toInt(), blend),
                                    light(brightness[i2].toInt(), blend),
                                    light(brightness[i3].toInt(), blend)
                                )
                            } else if (colours.textureIds == null || colours.textureIds!![index].toInt() == -1) {
                                // corners
                                val colour = colours.hsvColours!![index]
                                raster.drawGouraudTriangle(
                                    yOffset1,
                                    yOffset2,
                                    yOffset3,
                                    xOffset1,
                                    xOffset2,
                                    xOffset3,
                                    light(brightness[i1].toInt(), colour),
                                    light(brightness[i2].toInt(), colour),
                                    light(brightness[i3].toInt(), colour)
                                )
                            } else {
                                // water corners
                                val texture = textureColour(colours.textureIds?.get(index)?.toInt() ?: continue)
                                raster.drawGouraudTriangle(
                                    yOffset1,
                                    yOffset2,
                                    yOffset3,
                                    xOffset1,
                                    xOffset2,
                                    xOffset3,
                                    light(brightness[i1].toInt(), texture),
                                    light(brightness[i2].toInt(), texture),
                                    light(brightness[i3].toInt(), texture)
                                )
                            }
                        }
                    }
                }
                x -= SIZE
            }
            x = actualWidth
            y += SIZE
        }
    }

    fun calculateColours(
        x: Int,
        y: Int,
        xOffsets: IntArray,
        heightOffsets: IntArray?,
        yOffsets: IntArray,
        vertexIndices1: IntArray,
        vertexIndices2: IntArray,
        vertexIndices3: IntArray,
        colours: IntArray,
        blendColours: IntArray?,
        textures: IntArray,
        scales: IntArray
    ) {
        if (tileColours == null) {
            tileColours = Array(width) { arrayOfNulls(height) }
            textureColours = Array(width) { arrayOfNulls(height) }
        }
        var ignore = false
        if (colours.size == 2 && vertexIndices1.size == 2 && (colours[0] == colours[1] || textures[0] != -1 && textures[0] == textures[1])) {
            ignore = true
            for (i in 1..1) {
                val xOffset = xOffsets[vertexIndices1[i]]
                val yOffset = yOffsets[vertexIndices1[i]]
                if (xOffset != 0 && xOffset != tileUnits || yOffset != 0 && yOffset != tileUnits) {
                    ignore = false
                    break
                }
            }
        }
        if (!ignore) {
            val textureColour = TextureColours()
            val s = xOffsets.size
            val colourSize = colours.size
            textureColour.size = s.toShort()
            textureColour.brightness = ShortArray(s)
            textureColour.xOffsets = ShortArray(s)
            textureColour.heights = ShortArray(s)
            textureColour.yOffsets = ShortArray(s)
            for (index in 0 until s) {
                val xOffset = xOffsets[index]
                val yOffset = yOffsets[index]
                if (xOffset == 0 && yOffset == 0) {
                    textureColour.brightness!![index] = (tileBrightness[x][y] - tileShadows[x][y]).toShort()
                } else if (xOffset == 0 && yOffset == tileUnits) {
                    textureColour.brightness!![index] = (tileBrightness[x][y + 1] - tileShadows[x][y + 1]).toShort()
                } else if (xOffset == tileUnits && yOffset == tileUnits) {
                    textureColour.brightness!![index] = (tileBrightness[x + 1][y + 1] - tileShadows[x + 1][y + 1]).toShort()
                } else if (xOffset == tileUnits && yOffset == 0) {
                    textureColour.brightness!![index] = (tileBrightness[x + 1][y] - tileShadows[x + 1][y]).toShort()
                } else {
                    val dx = (tileBrightness[x][y] - tileShadows[x][y]) * (tileUnits - xOffset) + (tileBrightness[x + 1][y] - tileShadows[x + 1][y]) * xOffset
                    val dy = (tileBrightness[x][y + 1] - tileShadows[x][y + 1]) * (tileUnits - xOffset) + (tileBrightness[x + 1][y + 1] - tileShadows[x + 1][y + 1]) * xOffset
                    textureColour.brightness!![index] = (dx * (tileUnits - yOffset) + dy * yOffset shr 2 * tileScale).toShort()
                }
                val worldX = (x shl tileScale) + xOffset
                val worldY = (y shl tileScale) + yOffset
                textureColour.xOffsets!![index] = xOffset.toShort()
                textureColour.yOffsets!![index] = yOffset.toShort()
                textureColour.heights!![index] = (this.averageHeight(worldY, worldX) + if (heightOffsets != null) heightOffsets[index] else 0).toShort()
                if (textureColour.brightness!![index] < 2) {
                    textureColour.brightness!![index] = 2.toShort()
                }
            }
            var withoutTexture = false
            var colourCount = 0
            for (index in 0 until colourSize) {
                if (colours[index] >= 0 || blendColours != null && blendColours[index] >= 0) {
                    colourCount++
                }
                val id = textures[index]
                if (id != -1) {
                    val textureMetrics = materialDefinitions[id]
                    if (!textureMetrics.useTextureColour) {
                        withoutTexture = true
                        if (isTypeFourEightNine(textureMetrics.type.toInt()) || textureMetrics.aByte1211.toInt() != 0 || textureMetrics.aByte1203.toInt() != 0) {
                            textureColour.type = (textureColour.type.toInt() or 0x4).toByte()
                        }
                    }
                }
            }
            textureColour.hsvColours = IntArray(colourCount)
            if (blendColours != null) {
                textureColour.hsvBlendColours = IntArray(colourCount)
            }
            textureColour.vertexIndices1 = ShortArray(colourCount)
            textureColour.vertexIndices2 = ShortArray(colourCount)
            textureColour.vertexIndices3 = ShortArray(colourCount)
            if (withoutTexture) {
                textureColour.textureIds = ShortArray(colourCount)
                textureColour.scales = ShortArray(colourCount)
            }
            for (index in 0 until colourSize) {
                if (colours[index] >= 0 || blendColours != null && blendColours[index] >= 0) {
                    if (colours[index] >= 0) {
                        textureColour.hsvColours!![textureColour.count.toInt()] = hslToHsv(colours[index]).toInt()
                    } else {
                        textureColour.hsvColours!![textureColour.count.toInt()] = -1
                    }
                    if (blendColours != null) {
                        if (blendColours[index] == -1) {
                            textureColour.hsvBlendColours!![textureColour.count.toInt()] = -1
                        } else {
                            textureColour.hsvBlendColours!![textureColour.count.toInt()] = hslToHsv(blendColours[index]).toInt()
                        }
                    }
                    textureColour.vertexIndices1!![textureColour.count.toInt()] = vertexIndices1[index].toShort()
                    textureColour.vertexIndices2!![textureColour.count.toInt()] = vertexIndices2[index].toShort()
                    textureColour.vertexIndices3!![textureColour.count.toInt()] = vertexIndices3[index].toShort()
                    if (withoutTexture) {
                        if (textures[index] == -1 || materialDefinitions[textures[index]].useTextureColour) {
                            textureColour.textureIds!![textureColour.count.toInt()] = (-1).toShort()
                        } else {
                            textureColour.textureIds!![textureColour.count.toInt()] = textures[index].toShort()
                            textureColour.scales!![textureColour.count.toInt()] = scales[index].toShort()
                        }
                    }
                    textureColour.count++
                }
            }
            textureColours!![x][y] = textureColour
        } else if (colours[0] >= 0 || blendColours != null && blendColours[0] >= 0) {
            val tileColour = TileColours()
            val colour = colours[0]
            val texture = textures[0]
            if (blendColours != null) {
                tileColour.initialColourIndex = light(tileBrightness[x][y] - tileShadows[x][y], hslToHsv(blendColours[0]).toInt())
                if (colour == -1) {
                    tileColour.type = (tileColour.type.toInt() or 0x2).toByte()
                }
            }
            if (tile(x, y).height == tile(x + 1, y).height && tile(x, y).height == tile(x + 1, y + 1).height && tile(x, y).height == tile(x, y + 1).height) {
                tileColour.type = (tileColour.type.toInt() or 0x1).toByte()
            }
            var textureMetrics: MaterialDefinition? = null
            if (texture != -1) {
                textureMetrics = materialDefinitions[texture]
            }
            if (textureMetrics != null && tileColour.type.toInt() and 0x2 == 0 && !textureMetrics.useTextureColour) {
                tileColour.middleColourIndex = (tileBrightness[x][y] - tileShadows[x][y]).toShort()
                tileColour.eastColourIndex = (tileBrightness[x + 1][y] - tileShadows[x + 1][y]).toShort()
                tileColour.northEastColourIndex = (tileBrightness[x + 1][y + 1] - tileShadows[x + 1][y + 1]).toShort()
                tileColour.northColourIndex = (tileBrightness[x][y + 1] - tileShadows[x][y + 1]).toShort()
                tileColour.textureId = texture.toShort()
                if (isTypeFourEightNine(textureMetrics.type.toInt()) || textureMetrics.aByte1211.toInt() != 0 || textureMetrics.aByte1203.toInt() != 0) {
                    tileColour.type = (tileColour.type.toInt() or 0x4).toByte()
                }
            } else {
                val hsl = hslToHsv(colour).toInt()
                tileColour.middleColourIndex = light(tileBrightness[x][y] - tileShadows[x][y], hsl).toShort()
                tileColour.eastColourIndex = light(tileBrightness[x + 1][y] - tileShadows[x + 1][y], hsl).toShort()
                tileColour.northEastColourIndex = light(tileBrightness[x + 1][y + 1] - tileShadows[x + 1][y + 1], hsl).toShort()
                tileColour.northColourIndex = light(tileBrightness[x][y + 1] - tileShadows[x][y + 1], hsl).toShort()
                tileColour.textureId = (-1).toShort()
            }
            tileColours!![x][y] = tileColour
        }
    }

    companion object {
        val tileUnits: Int
        val tileScale: Int

        init {
            var scale = 0
            var units = 512
            while (units > 1) {
                scale++
                units = units shr 1
            }
            tileUnits = 1 shl scale
            tileScale = scale
        }

        private const val BRIGHTNESS = 75518
    }
}