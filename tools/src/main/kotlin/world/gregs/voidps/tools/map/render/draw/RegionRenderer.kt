package world.gregs.voidps.tools.map.render.draw

import world.gregs.voidps.cache.config.data.MapSceneDefinition
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull
import world.gregs.voidps.cache.definition.data.SpriteDefinition
import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.map.render.WorldMapDumper
import world.gregs.voidps.tools.map.render.load.MapTileSettings
import world.gregs.voidps.tools.map.render.load.RegionManager
import world.gregs.voidps.type.Region
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class RegionRenderer(
    private val manager: RegionManager,
    private val objectDecoder: Array<ObjectDefinitionFull>,
    private val spriteDecoder: Array<SpriteDefinition>,
    private val mapSceneDecoder: Array<MapSceneDefinition>,
    private val loader: MinimapIconPainter,
    private val settings: MapTileSettings,
    block: ((BufferedImage, Int, Region) -> Unit)? = null,
) : Pipeline.Modifier<Region> {

    private val block: (BufferedImage, Int, Region) -> Unit = block ?: { image, level, content ->
        val file = File("./images/$level/${content.id}.png")
        file.parentFile.mkdirs()
        ImageIO.write(image, "png", file)
    }

    override fun process(content: Region) {
        val start = System.currentTimeMillis()
        val regionX = content.x - 1
        val regionY = content.y - 1

        manager.loadTiles(regionX, regionY)
        settings.set(regionX, regionY)
        val objects = mutableMapOf<Int, List<MapObject>?>()
        for (rX in content.x - 1..content.x + 1) {
            for (rY in content.y - 1..content.y + 1) {
                val region = Region(rX, rY)
                objects[region.id] = manager.loadObjects(region)
            }
        }

        for (level in 0 until 4) {
            val img = manager.renderRegion(settings, level)

            val overlay = BufferedImage(manager.width * manager.scale, manager.height * manager.scale, BufferedImage.TYPE_INT_ARGB)
            val o = overlay.graphics as Graphics2D

            val painter = ObjectPainter(objectDecoder, spriteDecoder, mapSceneDecoder)
            painter.level = level
            painter.paint(o, Region(content.x, content.y), objects)

            if (WorldMapDumper.minimapIcons) {
                loader.paint(o, content, level, objects)
            }
            val g = img.graphics
            g.drawImage(overlay, 0, 1 + overlay.height, overlay.width, -overlay.height, null)

            try {
                val image = img.getSubimage(256, 257, 256, 256)
                if (isNotBlank(image)) {
                    block.invoke(image, level, content)
                    println("Written ${content.id} in ${System.currentTimeMillis() - start}ms")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
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
