package rs.dusk.tools.map.render.draw

import rs.dusk.cache.config.decoder.MapSceneDecoder
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.cache.definition.decoder.SpriteDecoder
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.obj.GameObjectLoc
import rs.dusk.tools.Pipeline
import rs.dusk.tools.map.render.load.MapTileSettings
import rs.dusk.tools.map.render.load.RegionManager
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class RegionRenderer(
    private val manager: RegionManager,
    private val objectDecoder: ObjectDecoder,
    private val spriteDecoder: SpriteDecoder,
    private val mapSceneDecoder: MapSceneDecoder,
    private val loader: MinimapIconPainter,
    private val settings: MapTileSettings
) : Pipeline.Modifier<Region> {

    override fun process(content: Region) {
        val start = System.currentTimeMillis()
        val regionX = content.x - 1
        val regionY = content.y - 1

        manager.loadTiles(regionX, regionY)
        settings.set(regionX, regionY)
        val img = manager.renderRegion(settings)

        val overlay = BufferedImage(manager.width * manager.scale, manager.height * manager.scale, BufferedImage.TYPE_INT_ARGB)
        val o = overlay.graphics as Graphics2D

        val objects = mutableMapOf<Int, List<GameObjectLoc>?>()
        for (rX in content.x - 1..content.x + 1) {
            for (rY in content.y - 1..content.y + 1) {
                val region = Region(rX, rY)
                objects[region.id] = manager.loadObjects(region)
            }
        }

        val painter = ObjectPainter(objectDecoder, spriteDecoder, mapSceneDecoder)
        painter.paint(o, Region(content.x, content.y), objects)

        loader.paint(o, content, 0, objects)
        val g = img.graphics
        g.drawImage(overlay, 0, 1 + overlay.height, overlay.width, -overlay.height, null)

        try {
            val image = img.getSubimage(256, 257, 256, 256)
            if (isNotBlank(image)) {
                ImageIO.write(image, "png", File("./images/${content.id}.png"))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        println("Written ${content.id} in ${System.currentTimeMillis() - start}ms")
    }

    companion object {
        private fun isNotBlank(img: BufferedImage): Boolean {
            loop@ for (x in 0 until img.width) {
                for (y in 0 until img.height) {
                    if (img.getRGB(x, y) != 0) {
                        return true
                    }
                }
            }
            return false
        }
    }
}