package world.gregs.voidps.tools.map.render.draw

import world.gregs.voidps.cache.config.data.MapSceneDefinition
import world.gregs.voidps.cache.definition.data.IndexedSprite
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull
import world.gregs.voidps.cache.definition.data.SpriteDefinition
import world.gregs.voidps.type.Region
import java.awt.Color
import java.awt.Graphics2D
import kotlin.math.ceil

class ObjectPainter(
    private val objectDefinitions: Array<ObjectDefinitionFull>,
    private val spriteDefinitions: Array<SpriteDefinition>,
    private val mapSceneDefinitions: Array<MapSceneDefinition>,
) {

    var offsetX = 0
    var offsetY = 0
    var level = 0

    val drawWalls = true
    val drawMapScenes = true

    fun getScale() = 4

    fun canvasX(x: Int): Int = x * getScale()

    fun canvasY(y: Int): Int = y * getScale()

    private fun Graphics2D.drawLine(x: Int, y: Int, xOffset: Int, yOffset: Int, width: Int, height: Int, colour: Int) {
        color = Color(colour)
        val pixelSize = getScale() / 4.0
        fillRect(ceil(canvasX(x) + xOffset * pixelSize).toInt(), ceil(canvasY(y) + yOffset * pixelSize).toInt(), ceil(pixelSize * width).toInt(), ceil(pixelSize * height).toInt())
    }

    private fun Graphics2D.drawLine(x: Int, y: Int, width: Int, height: Int, colour: Int) {
        color = Color(colour)
        val pixelSize = getScale() / 4.0
        fillRect(canvasX(x), canvasY(y), ceil(pixelSize * width).toInt(), ceil(pixelSize * height).toInt())
    }

    private fun Graphics2D.drawDiagonalLine(x: Int, y: Int, colour: Int, flip: Boolean) {
        color = Color(colour)
        val pixelSize = getScale() / 4.0
        val canvasX = canvasX(x)
        val canvasY = canvasY(y)
        for (size in 0 until 4) {
            fillRect(ceil(canvasX + pixelSize * size).toInt(), ceil(canvasY + (pixelSize * (if (flip) 3 - size else size))).toInt(), ceil(pixelSize).toInt(), ceil(pixelSize).toInt())
        }
    }

    fun drawDiagonalCorner(g: Graphics2D, definition: ObjectDefinitionFull, baseX: Int, baseY: Int, type: Int, rotation: Int) {
        if (definition.mapscene != -1) {
            method2343(definition, rotation, g, baseX, baseY)
        } else if (type == 9 && drawWalls) {
            var colour = WHITE
            if (definition.interactive > 0) {
                colour = RED
            }
            if (rotation == 0 || rotation == 2) {
                g.drawDiagonalLine(baseX, baseY, colour, false)
            } else {
                g.drawDiagonalLine(baseX, baseY, colour, true)
            }
        }
    }

    fun drawWall(graphicstoolkit: Graphics2D, definition: ObjectDefinitionFull, baseX: Int, baseY: Int, type: Int, rotation: Int) {
        if (definition.mapscene == -1) {
            if (!drawWalls) {
                return
            }
            var colour = WHITE
            if (definition.interactive > 0) {
                colour = RED
            }
            if (type == 0 || type == 2) {
                when (rotation) {
                    0 -> graphicstoolkit.drawLine(baseX, baseY, 1, 4, colour)
                    1 -> graphicstoolkit.drawLine(baseX, baseY, 0, 3, 4, 1, colour)
                    2 -> graphicstoolkit.drawLine(baseX, baseY, 3, 0, 1, 4, colour)
                    else -> graphicstoolkit.drawLine(baseX, baseY, 4, 1, colour)
                }
            }
            if (type == 3) {
                when (rotation) {
                    0 -> graphicstoolkit.drawLine(baseX, baseY, 0, 3, 1, 1, colour)
                    1 -> graphicstoolkit.drawLine(baseX, baseY, 3, 3, 1, 1, colour)
                    2 -> graphicstoolkit.drawLine(baseX, baseY, 3, 0, 1, 1, colour)
                    else -> graphicstoolkit.drawLine(baseX, baseY, 0, 0, 1, 1, colour)
                }
            }
            if (type == 2) {
                when (rotation) {
                    0 -> graphicstoolkit.drawLine(baseX, baseY, 0, 3, 4, 1, colour)
                    1 -> graphicstoolkit.drawLine(baseX, baseY, 3, 0, 1, 4, colour)
                    2 -> graphicstoolkit.drawLine(baseX, baseY, 0, 0, 4, 1, colour)
                    else -> graphicstoolkit.drawLine(baseX, baseY, 1, 4, colour)
                }
            }
        } else {
            method2343(definition, type, graphicstoolkit, baseX, baseY)
        }
    }

    fun method2343(objectdefinition: ObjectDefinitionFull, settings: Int, graphicstoolkit: Graphics2D, baseX: Int, baseY: Int) {
        if (!drawMapScenes) {
            return
        }
        var settings = settings
        val mapSceneDefinition = mapSceneDefinitions.getOrNull(objectdefinition.mapscene)
        if (mapSceneDefinition != null && mapSceneDefinition.sprite != -1) {
            if (objectdefinition.aBoolean3056) {
                settings += objectdefinition.anInt2958
                settings = settings and 0x3
            } else {
                settings = 0
            }
            val glsprite = mapSceneDefinition.method1606(settings, objectdefinition.invertMapScene)
            if (glsprite != null) {
                var sizeX = objectdefinition.sizeX
                var sizeY = objectdefinition.sizeY
                if (settings and 0x1 == 1) {
                    sizeX = objectdefinition.sizeY
                    sizeY = objectdefinition.sizeX
                }
                var width = glsprite.deltaWidth + glsprite.width + glsprite.offsetX
                var height = glsprite.deltaHeight + glsprite.height + glsprite.offsetY
                if (mapSceneDefinition.aBoolean1741) {
                    height = sizeY * 4
                    width = sizeX * 4
                }
                graphicstoolkit.drawSprite(baseX, baseY + sizeY, glsprite, width, height)
            }
        }
    }

    fun Graphics2D.drawSprite(baseX: Int, baseY: Int, sprite: IndexedSprite, width: Int, height: Int) {
        val baseX = canvasX(baseX)
        val baseY = canvasY(baseY)
        val spriteSize = getScale() / 4.0
        for (x in 0 until width) {
            for (y in 0 until height) {
                val i = x + y * width
                if (sprite.alpha == null) {
                    val colour = sprite.palette[sprite.raster[i].toInt() and 255]
                    if (colour != 0) {
                        color = Color(-16777216 or colour)
                        fillRect(ceil(baseX + (x * spriteSize)).toInt(), ceil(baseY - (y * spriteSize)).toInt(), ceil(spriteSize).toInt(), ceil(spriteSize).toInt())
                    }
                } else {
                    color = Color(sprite.palette[sprite.raster[i].toInt() and 255] or (sprite.alpha!![i].toInt() shl 24))
                    fillRect(ceil(baseX + (x * spriteSize)).toInt(), ceil(baseY - (y * spriteSize)).toInt(), ceil(spriteSize).toInt(), ceil(spriteSize).toInt())
                }
            }
        }
    }

    internal fun MapSceneDefinition.method1606(setting: Int, bool: Boolean): IndexedSprite? {
        val image = spriteDefinitions[sprite].sprites?.first()
        if (image != null) {
            image.offsetY = 0
            image.deltaWidth = image.offsetY
            image.offsetX = image.deltaWidth
            image.deltaHeight = image.offsetX
            if (bool) {
                image.method4189()
            }
            for (i in 0 until setting) {
                image.method4198()
            }
        }
        return image
    }

    fun drawObject(g: Graphics2D, region: Region, regionId: Int, obj: MapObject, definition: ObjectDefinitionFull) {
        if (definition.hideMinimap) {
            return
        }
        if (obj.level != level) { // FIXME should render more than one level at once
            return
        }
        offsetX = Region.x(regionId) - region.x + 1
        offsetY = Region.y(regionId) - region.y + 1
        val localX = obj.x
        val localY = obj.y
        val rotation = obj.rotation
        val type = obj.shape
        val bool65 = definition.animations == null && definition.transforms == null && !definition.aBoolean2998 && !definition.aBoolean2992
        val baseX = (offsetX * 64) + localX
        val baseY = (offsetY * 64) + localY
        if (type == 22) {
            if (definition.interactive != 0 || definition.solid == 1 || definition.blocksLand) {
                // Map scene
                if (definition.mapscene != -1) {
                    method2343(definition, rotation, g, baseX, baseY)
                }
            }
        } else if (type == 10 || type == 11) {
            drawDiagonalCorner(g, definition, baseX, baseY, type, rotation)
        } else if (type in 12..17 || type in 18..21) {
            drawDiagonalCorner(g, definition, baseX, baseY, type, rotation)
        } else if (type == 0) {
            drawWall(g, definition, baseX, baseY, type, rotation)
        } else if (type == 1) {
            drawWall(g, definition, baseX, baseY, type, rotation)
        } else if (type == 2) {
            drawWall(g, definition, baseX, baseY, type, rotation + if (!bool65) 4 else 0)
        } else if (type == 3) {
            drawWall(g, definition, baseX, baseY, type, rotation)
        } else if (type == 9) {
            drawDiagonalCorner(g, definition, baseX, baseY, type, rotation)
        }
    }

    fun paint(g: Graphics2D, region: Region, objects: Map<Int, List<MapObject>?>) {
        objects.forEach { (regionId, list) ->
            list?.forEach { obj ->
                val definition = objectDefinitions.getOrNull(obj.id)
                if (definition != null) {
                    drawObject(g, region, regionId, obj, definition)
                }
            }
        }
    }

    fun IndexedSprite.method4189() {
        val bs = raster
        if (alpha == null) {
            for (i in (height shr 1) - 1 downTo 0) {
                var i24 = i * width
                var i25 = (height - i - 1) * width
                for (index in -width..-1) {
                    val b = bs[i24]
                    bs[i24] = bs[i25]
                    bs[i25] = b
                    i24++
                    i25++
                }
            }
        } else {
            val alpha = alpha
            for (i in (height shr 1) - 1 downTo 0) {
                var i28 = i * width
                var i29 = (height - i - 1) * width
                for (i30 in -width..-1) {
                    var b = bs[i28]
                    bs[i28] = bs[i29]
                    bs[i29] = b
                    b = alpha!![i28]
                    alpha[i28] = alpha[i29]
                    alpha[i29] = b
                    i28++
                    i29++
                }
            }
        }
        val i = offsetY
        offsetY = deltaHeight
        deltaHeight = i
    }

    fun IndexedSprite.method4198() {
        val bs = ByteArray(width * height)
        var i = 0
        if (alpha == null) {
            for (j in 0 until width) {
                for (k in height - 1 downTo 0) {
                    bs[i++] = raster[j + k * width]
                }
            }
            raster = bs
        } else {
            val bytes = ByteArray(width * height)
            for (j in 0 until width) {
                for (k in height - 1 downTo 0) {
                    bs[i] = raster[j + k * width]
                    bytes[i++] = alpha!![j + k * width]
                }
            }
            raster = bs
            alpha = bytes
        }
        offsetY = offsetX
        offsetX = deltaHeight
        deltaHeight = deltaWidth
        deltaWidth = offsetY
        val temp: Int = height
        height = width
        width = temp
    }

    companion object {
        const val WHITE = -1118482
        const val RED = -1179648
    }
}
