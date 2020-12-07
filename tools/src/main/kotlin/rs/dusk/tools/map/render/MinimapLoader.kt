package rs.dusk.tools.map.render

import rs.dusk.cache.Cache
import rs.dusk.cache.Indices.DEFAULTS
import rs.dusk.cache.Indices.WORLD_MAP
import rs.dusk.cache.config.data.WorldMapInfoDefinition
import rs.dusk.cache.config.decoder.WorldMapInfoDecoder
import rs.dusk.cache.definition.data.IndexedSprite
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.cache.definition.decoder.SpriteDecoder
import rs.dusk.cache.definition.decoder.WorldMapDecoder
import rs.dusk.core.io.read.BufferReader
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.obj.GameObjectLoc
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class MinimapLoader(
    val objectDefinitions: ObjectDecoder,
    val worldMapDefinitions: WorldMapDecoder,
    val worldMapInfoDefinitions: WorldMapInfoDecoder,
    val spriteDefinitions: SpriteDecoder
) {

    data class MapIcon(val x: Int, val y: Int, val plane: Int, val bi: BufferedImage)

    val images = ConcurrentLinkedQueue<MapIcon>()

    val nameAreas = mutableMapOf<Int, MutableList<WorldMapInfoDefinition>>()
    val backgrounds = ConcurrentHashMap<Int, List<BufferedImage>>()

    fun startup(cache: Cache) {
        images.clear()
        val stringId = "${worldMapDefinitions.get(DEFAULTS).map}_staticelements"

        val archiveId = cache.getArchiveId(WORLD_MAP, stringId)
        var length = cache.archiveCount(WORLD_MAP, archiveId)
        println("Length $length")
        val positions = IntArray(length)
        val ids = IntArray(length)

        val aBoolean1313 = false
        var counter = 0
        var index = 0
        while (length > counter) {
            val file = cache.getFile(WORLD_MAP, archiveId, index++) ?: continue
            val buffer = BufferReader(file)
            val position = buffer.readInt()
            val id = buffer.readShort()
            val type = buffer.readUnsignedByte()
            if (aBoolean1313 && type == 1) {
                length--
            } else {
                positions[counter] = position
                ids[counter] = id
                counter++
            }
        }

        for (i in 0 until length) {
            val x = positions[i] shr 14 and 0x3fff
            val y = positions[i] and 0x3fff
            val plane = positions[i] shr 28 and 0x3
            val mapDef = worldMapDefinitions.get(28)
            if (mapDef.sections != null) {
                for (it in mapDef.sections!!) {
                    if (plane == it.plane && it.minX <= x && x <= it.maxX && y >= it.minY && y <= it.maxY) {
                        val key = hash(x, y, plane)
                        val list = nameAreas.getOrPut(key) { mutableListOf() }
                        list.add(worldMapInfoDefinitions.get(ids[i]))
                        break
                    }
                }
            }
        }
    }

    fun hash(x: Int, y: Int, plane: Int) = y + (x shl 14) + (plane shl 28)

    fun loadRegion(g: Graphics, region: Region, plane: Int, objects: List<GameObjectLoc>?) {
        loadIcons(region.x, region.y, objects)

        val iconScale = 2
        images.forEach {
            val width = it.bi.width * iconScale
            val height = it.bi.height * iconScale
            val regionX = region.x * 64
            val regionY = region.y * 64
            if (it.x in regionX..regionX + 64 && it.y in regionY..regionY + 64 && it.plane == plane) {
                g.drawImage(it.bi, (it.x - regionX) * 4, (it.y - regionY) * 4, width, height, null)
            }
        }
//        backgrounds[region.regionId] =
//            (0 until 4).map { plane -> Render.getRegionImage(region.regionId, plane).getSubimage(0, 1, 256, 256) }
    }

    fun loadIcons(regionX: Int, regionY: Int, objects: List<GameObjectLoc>?) {
        objects?.forEach {
            val definition = objectDefinitions.getOrNull(it.id) ?: return@forEach
            if (definition.mapDefinitionId != -1) {
                val mapInfo = worldMapInfoDefinitions.get(definition.mapDefinitionId)
                val sprite = mapInfo.toSprite(false)
                if (sprite != null) {
                    images.add(
                        MapIcon(
                            regionX * 64 + it.localX,
                            regionY * 64 + it.localY,
                            it.plane,
                            sprite.toBufferedImage()
                        )
                    )
                }
                if (mapInfo.aBoolean1079) {
                    if (mapInfo.name != null) {
                        println("Map info text: ${mapInfo.name}")
                    }
                }
            }
        }
    }
    fun WorldMapInfoDefinition.toSprite(bool: Boolean): IndexedSprite? {
        val i = if (!bool) anInt1062 else anInt1056
        return if (i > 0) spriteDefinitions.get(i).sprites?.firstOrNull() else null
    }

    fun IndexedSprite.toBufferedImage() : BufferedImage {
        val bi = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val i = x + y * width
                if(alpha == null) {
                    val colour = palette[raster[i].toInt() and 255]
                    if(colour != 0)
                        bi.setRGB(x, y, -16777216 or colour)
                } else {
                    bi.setRGB(x, y, palette[raster[i].toInt() and 255] or (alpha!![i].toInt() shl 24))
                }
            }
        }
        return bi
    }

}