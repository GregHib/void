package world.gregs.voidps.tools.cache

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.SpriteDecoder
import world.gregs.voidps.tools.property
import java.io.File
import javax.imageio.ImageIO

object DumpSprites {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("storage.cache.path"))
        val decoder = SpriteDecoder().load(cache)
        println(decoder.lastIndex)
        val directory = File("./temp/sprites/")
        directory.mkdir()
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            println("Sprite $i ${def.sprites?.size}")
            val sprites = def.sprites ?: continue
            for ((index, sprite) in sprites.withIndex()) {
                if (sprite.width > 0 && sprite.height > 0) {
                    ImageIO.write(sprite.toBufferedImage(), "png", directory.resolve("${i}_${index}.png"))
                }
            }
        }
    }
}