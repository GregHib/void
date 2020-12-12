package rs.dusk.tools.map.process

import org.koin.core.context.startKoin
import rs.dusk.cache.Cache
import rs.dusk.cache.config.data.WorldMapInfoDefinition
import rs.dusk.cache.config.decoder.WorldMapInfoDecoder
import rs.dusk.cache.definition.data.IndexedSprite
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.cache.definition.decoder.SpriteDecoder
import rs.dusk.engine.client.cacheConfigModule
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.region.obj.*
import rs.dusk.engine.map.region.tile.TileDecoder
import rs.dusk.tools.map.render.draw.MinimapIconPainter
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

object PreProcessMap {


    class IconLoader(private val objectDecoder: ObjectDecoder, private val mapInfoDecoder: WorldMapInfoDecoder, private val spriteDecoder: SpriteDecoder) {

        fun loadIcons(regionX: Int, regionY: Int, objects: List<GameObjectLoc>?): Map<Int, MinimapIconPainter.MapIcon> {
            val images = mutableMapOf<Int, MinimapIconPainter.MapIcon>()
            objects?.forEach {
                val definition = objectDecoder.getOrNull(it.id) ?: return@forEach
                if (definition.mapDefinitionId != -1) {
                    val mapInfo = mapInfoDecoder.get(definition.mapDefinitionId)
                    val sprite = mapInfo.toSprite(false)
                    if (sprite != null) {
                        val x = regionX * 64 + it.localX
                        val y = regionY * 64 + it.localY
                        images[Tile.getId(it.localX, it.localY, it.plane)] = MinimapIconPainter.MapIcon(x, y, it.plane, sprite.toBufferedImage())
                    }
                }
            }
            return images
        }

        private fun WorldMapInfoDefinition.toSprite(bool: Boolean): IndexedSprite? {
            val i = if (!bool) spriteId else highlightSpriteId
            return if (i > 0) spriteDecoder.get(i).sprites?.firstOrNull() else null
        }

        private fun IndexedSprite.toBufferedImage(): BufferedImage {
            val bi = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    val i = x + y * width
                    if (alpha == null) {
                        val colour = palette[raster[i].toInt() and 255]
                        if (colour != 0) {
                            bi.setRGB(x, y, -16777216 or colour)
                        }
                    } else {
                        bi.setRGB(x, y, palette[raster[i].toInt() and 255] or (alpha!![i].toInt() shl 24))
                    }
                }
            }
            return bi
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule, cacheConfigModule, xteaModule, objectMapDecoderModule)
        }.koin

        val cache: Cache = koin.get()
        val xteas: Xteas = koin.get()
        val tileDecoder = TileDecoder()
        val objectDecoder: ObjectDecoder = koin.get()
        val mapObjDecoder: GameObjectMapDecoder = koin.get()
        val mapInfoDecoder: WorldMapInfoDecoder = koin.get()
        val spriteDecoder: SpriteDecoder = koin.get()
        val set = mutableSetOf<Int>()
        File("./icons/").mkdir()
        for(id in mapInfoDecoder.indices) {
            val def = mapInfoDecoder.get(id)
            val spriteId = def.spriteId
            if(set.contains(spriteId)) {
                continue
            }
            set.add(spriteId)
            val sprite = if (spriteId > 0) spriteDecoder.get(spriteId).sprites?.firstOrNull() else null
            val image = sprite?.toBufferedImage() ?: continue
            try {
                ImageIO.write(image, "png", File("./icons/${spriteId}.png"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

//        val loader = IconLoader(objectDecoder, mapInfoDecoder, spriteDecoder)
//        File("./images/").mkdir()
//        val pipeline = Pipeline<Region>()
//        pipeline.add(ObjectProcessor(tileDecoder, mapObjDecoder, objectDecoder, xteas, cache, loader))
//
//        val regions = mutableListOf<Region>()
//        for (regionX in 0 until 256) {
//            for (regionY in 0 until 256) {
//                cache.getFile(5, "m${regionX}_${regionY}") ?: continue
//                regions.add(Region(regionX, regionY))
//            }
//        }
//        val start = System.currentTimeMillis()
//        regions.forEach {
//            pipeline.process(it)
//        }
//        println("${regions.size} regions loaded in ${System.currentTimeMillis() - start}ms")
    }


}