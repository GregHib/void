package rs.dusk.tools.map.render

import rs.dusk.cache.definition.data.TextureDefinition
import rs.dusk.cache.definition.decoder.TextureDecoder
import rs.dusk.engine.map.region.tile.TileData
import rs.dusk.tools.map.render.Render.size
import rs.dusk.tools.map.render.raster.Raster
import world.gregs.hestia.map.model.render.TextureColours
import world.gregs.hestia.map.model.render.TileColours
import kotlin.math.sqrt

class JavaPlane(
    private val textureDefinitions: TextureDecoder,
    private val settings: Int,
    width: Int,
    height: Int,
    tiles: Array<Array<TileData?>>
) : Plane(width, height, tiles) {

    private val tileBrightness = Array(width + 1) { ByteArray(height + 1) }
    private val tileShadows = Array(width + 1) { ByteArray(height + 1) }

    //Java toolkit stuff
    private var tileColours: Array<Array<TileColours?>>? = null
    private var textureColours: Array<Array<TextureColours?>>? = null
    var lightX: Int = 0
    var lightY: Int = 0
    var lightZ: Int = 0

    fun setBrightness(f_246_: Float, f_247_: Float, f_248_: Float) {
        val f_249_ = sqrt((f_246_ * f_246_ + f_247_ * f_247_ + f_248_ * f_248_).toDouble()).toFloat()
        lightX = (f_246_ * 65535.0f / f_249_).toInt()// -36578
        lightY = (f_247_ * 65535.0f / f_249_).toInt()// -40235
        lightZ = (f_248_ * 65535.0f / f_249_).toInt()// -36578
    }


    companion object {
        const val brightness = 75518
    }

    init {
        setBrightness(-200f, -220f, -200f)
        val brightness: Int = brightness shr 9
        for (y in 1 until height) {
            for (x in 1 until width) {
                var intensity = brightness
                val dx = (tiles[x + 1][y]?.height ?: 0) - (tiles[x - 1][y]?.height ?: 0)
                val dy = (tiles[x][y + 1]?.height ?: 0) - (tiles[x][y - 1]?.height ?: 0)
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

        //Fixed for blurred edges
        for(y in 0..height) {
            tileBrightness[0][y] = 126.toByte()
            tileBrightness[width][y] = 126.toByte()
        }
        for(x in 0..width) {
            tileBrightness[x][0] = 126.toByte()
            tileBrightness[x][height] = 126.toByte()
        }
    }

    fun hslToHsv(hsl: Int): Short {
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

    fun isTypeFourEightNine(i: Int): Boolean {
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

    fun textureColour(i: Int): Int {
        return textureDefinitions.get(i).colour and 0xffff
    }

    fun light(light: Int, colour: Int): Int {
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

    fun drawTiles(startX: Int, startY: Int, endX: Int, endY: Int, useUnderlay: Array<BooleanArray>, raster: Raster, xOffsets: IntArray, yOffsets: IntArray) {
        val actualWidth = (endY - startY) * 1024 / 256// width * 4
        var y = 0
        var x = actualWidth
        for (localX in startX until endX) {
            for (localY in startY until endY) {
                if (useUnderlay[localX - startX][localY - startY]) {
                    if (tileColours?.get(localX)?.get(localY) != null) {
                        val tileColours = tileColours!![localX][localY]!!
                        if (tileColours.textureId.toInt() != -1 && tileColours.type.toInt() and 0x2 == 0 && tileColours.initialColourIndex == -1) {
                            val textureColour = textureColour(tileColours.textureId.toInt())
                            // water
                            raster.drawGouraudTriangle(
                                (x - size).toFloat(),
                                (x - size).toFloat(),
                                x.toFloat(),
                                (y + size).toFloat(),
                                y.toFloat(),
                                (y + size).toFloat(),
                                light(tileColours.northEastBrightness.toInt() and 0xffff, textureColour).toFloat(),
                                light(tileColours.northColourIndex.toInt() and 0xffff, textureColour).toFloat(),
                                light(tileColours.eastColourIndex.toInt() and 0xffff, textureColour).toFloat()
                            )
                            raster.drawGouraudTriangle(
                                x.toFloat(),
                                x.toFloat(),
                                (x - size).toFloat(),
                                y.toFloat(),
                                (y + size).toFloat(),
                                y.toFloat(),
                                light(tileColours.middleColourIndex.toInt() and 0xffff, textureColour).toFloat(),
                                light(tileColours.eastColourIndex.toInt() and 0xffff, textureColour).toFloat(),
                                light(tileColours.northColourIndex.toInt() and 0xffff, textureColour).toFloat()
                            )
                        } else if (tileColours.initialColourIndex == -1) {
                            // normal tiles
                            raster.drawGouraudTriangle(
                                (x - size).toFloat(),
                                (x - size).toFloat(),
                                x.toFloat(),
                                (y + size).toFloat(),
                                y.toFloat(),
                                (y + size).toFloat(),
                                (tileColours.northEastBrightness.toInt() and 0xffff).toFloat(),
                                (tileColours.northColourIndex.toInt() and 0xffff).toFloat(),
                                (tileColours.eastColourIndex.toInt() and 0xffff).toFloat()
                            )
                            raster.drawGouraudTriangle(
                                x.toFloat(),
                                x.toFloat(),
                                (x - size).toFloat(),
                                y.toFloat(),
                                (y + size).toFloat(),
                                y.toFloat(),
                                (tileColours.middleColourIndex.toInt() and 0xffff).toFloat(),
                                (tileColours.eastColourIndex.toInt() and 0xffff).toFloat(),
                                (tileColours.northColourIndex.toInt() and 0xffff).toFloat()
                            )
                        } else {
                            // overlay
                            val brightness = tileColours.initialColourIndex
                            raster.drawGouraudTriangle((x - size).toFloat(), (x - size).toFloat(), x.toFloat(), (y + size).toFloat(), y.toFloat(), (y + size).toFloat(), brightness.toFloat(), brightness.toFloat(), brightness.toFloat())
                            raster.drawGouraudTriangle(x.toFloat(), x.toFloat(), (x - size).toFloat(), y.toFloat(), (y + size).toFloat(), y.toFloat(), brightness.toFloat(), brightness.toFloat(), brightness.toFloat())
                        }
                    } else if (textureColours?.get(localX)?.get(localY) != null) {
                        val textureColour = textureColours!![localX][localY]!!
                        for (index in 0 until textureColour.size) {
                            xOffsets[index] = y + textureColour.xOffsets!![index] * size / tileUnits
                            yOffsets[index] = x - textureColour.yOffsets!![index] * size / tileUnits
                        }
                        for (index in 0 until textureColour.count) {
                            val i1 = textureColour.vertexIndices1!![index]
                            val i2 = textureColour.vertexIndices2!![index]
                            val i3 = textureColour.vertexIndices3!![index]
                            val xOffset1 = xOffsets[i1.toInt()]
                            val xOffset2 = xOffsets[i2.toInt()]
                            val xOffset3 = xOffsets[i3.toInt()]
                            val yOffset1 = yOffsets[i1.toInt()]
                            val yOffset2 = yOffsets[i2.toInt()]
                            val yOffset3 = yOffsets[i3.toInt()]
                            if (textureColour.hsvBlendColours != null && textureColour.hsvBlendColours!![index] != -1) {
                                // overlay corners
                                val blend = textureColour.hsvBlendColours!![index]
                                raster.drawGouraudTriangle(
                                    yOffset1.toFloat(),
                                    yOffset2.toFloat(),
                                    yOffset3.toFloat(),
                                    xOffset1.toFloat(),
                                    xOffset2.toFloat(),
                                    xOffset3.toFloat(),
                                    light(textureColour.brightness!![i1.toInt()].toInt(), blend).toFloat(),
                                    light(textureColour.brightness!![i2.toInt()].toInt(), blend).toFloat(),
                                    light(textureColour.brightness!![i3.toInt()].toInt(), blend).toFloat()
                                )
                            } else if (textureColour.textureIds == null || textureColour.textureIds!![index].toInt() == -1) {
                                // corners
                                val colour = textureColour.hsvColours!![index]
                                raster.drawGouraudTriangle(
                                    yOffset1.toFloat(),
                                    yOffset2.toFloat(),
                                    yOffset3.toFloat(),
                                    xOffset1.toFloat(),
                                    xOffset2.toFloat(),
                                    xOffset3.toFloat(),
                                    light(textureColour.brightness!![i1.toInt()].toInt(), colour).toFloat(),
                                    light(textureColour.brightness!![i2.toInt()].toInt(), colour).toFloat(),
                                    light(textureColour.brightness!![i3.toInt()].toInt(), colour).toFloat()
                                )
                            } else {
                                // water corners
                                val texture = textureColour(textureColour.textureIds?.get(index)?.toInt() ?: continue)
                                raster.drawGouraudTriangle(
                                    yOffset1.toFloat(),
                                    yOffset2.toFloat(),
                                    yOffset3.toFloat(),
                                    xOffset1.toFloat(),
                                    xOffset2.toFloat(),
                                    xOffset3.toFloat(),
                                    light(textureColour.brightness!![i1.toInt()].toInt(), texture).toFloat(),
                                    light(textureColour.brightness!![i2.toInt()].toInt(), texture).toFloat(),
                                    light(textureColour.brightness!![i3.toInt()].toInt(), texture).toFloat()
                                )
                            }
                        }
                    }
                }
                x -= size
            }
            x = actualWidth
            y += size
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
            tileColours = Array(width) { arrayOfNulls<TileColours>(height) }
            textureColours = Array(width) { arrayOfNulls<TextureColours>(height) }
        }
        var bool_325_ = false
        if (colours.size == 2 && vertexIndices1.size == 2 && (colours[0] == colours[1] || textures[0] != -1 && textures[0] == textures[1])) {
            bool_325_ = true
            for (i_326_ in 1..1) {
                val xOffset = xOffsets[vertexIndices1[i_326_]]
                val yOffset = yOffsets[vertexIndices1[i_326_]]
                if (xOffset != 0 && xOffset != tileUnits || yOffset != 0 && yOffset != tileUnits) {
                    bool_325_ = false
                    break
                }
            }
        }
        if (!bool_325_) {
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
                    val textureMetrics = textureDefinitions.get(id)
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
                        if (textures[index] == -1 || textureDefinitions.get(textures[index]).useTextureColour) {
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
            if (tiles[x][y] == tiles[x + 1][y] && tiles[x][y] == tiles[x + 1][y + 1] && tiles[x][y] == tiles[x][y + 1]) {
                tileColour.type = (tileColour.type.toInt() or 0x1).toByte()
            }
            var textureMetrics: TextureDefinition? = null
            if (texture != -1) {
                textureMetrics = textureDefinitions.get(texture)
            }
            if (textureMetrics != null && tileColour.type.toInt() and 0x2 == 0 && !textureMetrics.useTextureColour) {
                tileColour.middleColourIndex = (tileBrightness[x][y] - tileShadows[x][y]).toShort()
                tileColour.eastColourIndex = (tileBrightness[x + 1][y] - tileShadows[x + 1][y]).toShort()
                tileColour.northEastBrightness = (tileBrightness[x + 1][y + 1] - tileShadows[x + 1][y + 1]).toShort()
                tileColour.northColourIndex = (tileBrightness[x][y + 1] - tileShadows[x][y + 1]).toShort()
                tileColour.textureId = texture.toShort()
                if (isTypeFourEightNine(textureMetrics.type.toInt()) || textureMetrics.aByte1211.toInt() != 0 || textureMetrics.aByte1203.toInt() != 0) {
                    tileColour.type = (tileColour.type.toInt() or 0x4).toByte()
                }
            } else {
                val s = hslToHsv(colour)
                tileColour.middleColourIndex = light(tileBrightness[x][y] - tileShadows[x][y], s.toInt()).toShort()
                tileColour.eastColourIndex = light(tileBrightness[x + 1][y] - tileShadows[x + 1][y], s.toInt()).toShort()
                tileColour.northEastBrightness = light(tileBrightness[x + 1][y + 1] - tileShadows[x + 1][y + 1], s.toInt()).toShort()
                tileColour.northColourIndex = light(tileBrightness[x][y + 1] - tileShadows[x][y + 1], s.toInt()).toShort()
                tileColour.textureId = (-1).toShort()
            }
            tileColours!![x][y] = tileColour
        }
    }
}