package rs.dusk.tools.map.render

import org.koin.core.context.startKoin
import rs.dusk.cache.Cache
import rs.dusk.cache.config.decoder.WorldMapInfoDecoder
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.cache.definition.decoder.SpriteDecoder
import rs.dusk.cache.definition.decoder.WorldMapDecoder
import rs.dusk.engine.client.cacheConfigModule
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.obj.GameObjectMapDecoder
import rs.dusk.engine.map.region.obj.Xteas
import rs.dusk.engine.map.region.obj.objectMapDecoderModule
import rs.dusk.engine.map.region.obj.xteaModule
import rs.dusk.engine.map.region.tile.TileDecoder
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

object RenderMinimapIcons {

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule, cacheConfigModule, xteaModule, objectMapDecoderModule, objectMapDecoderModule)
        }.koin

        val cache: Cache = koin.get()
        val start = System.currentTimeMillis()
        val images = File("./images/").listFiles()
        val regions = images?.map {
            val id = it.nameWithoutExtension.toInt()
            Region(id) to it
        } ?: return
        val objDecoder: ObjectDecoder = koin.get()
        val worldMapDecoder: WorldMapDecoder = koin.get()
        val worldMapInfoDecoder: WorldMapInfoDecoder = koin.get()
        val spriteDecoder: SpriteDecoder = koin.get()
        val xteas: Xteas = koin.get()
        val tileDecoder = TileDecoder()
        val mapObjDecoder: GameObjectMapDecoder = koin.get()
        val loader = MinimapLoader(objDecoder, worldMapDecoder, worldMapInfoDecoder, spriteDecoder)
        loader.startup(cache)

        val region = Region(49, 54)
        val img = BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB)
        val g = img.graphics

        val mapData = cache.getFile(5, "m${region.x}_${region.y}") ?: return
        val xtea = xteas[region.id]
        val locationData = cache.getFile(5, "l${region.x}_${region.y}", xtea)

        if (locationData == null) {
            println("Missing xteas for region $region.id [${xtea?.toList()}].")
            return
        }

        val tiles = tileDecoder.read(mapData)
        val objects = mapObjDecoder.read(locationData, tiles)

        loader.loadRegion(g, region, 0, objects)

        try {
            ImageIO.write(img, "png", File("./overlay.png"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
//        val iconScale = 2.0
//        for ((region, file) in regions) {
//            println("Stitching region ${region.id}")
//            val img = ImageIO.read(file)
//            val g = img.graphics
//
//            val width = img.width * iconScale
//            val height = img.height * iconScale
//            if (it.x in visibleRegionX(width) && it.y in swing.visibleRegionY(height) && it.plane == swing.plane) {
//                g.drawImage(icon, swing.canvasX(it.x), swing.realCanvasY(it.y), width, height, null)
//            }
//        }
    }
}