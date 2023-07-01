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
        val cache: Cache = CacheDelegate(property("cachePath"))
        val decoder = SpriteDecoder(cache)
        println(decoder.last)
        File("./sprites/").mkdir()
        repeat(decoder.last) { i ->
            val def = decoder.getOrNull(i) ?: return@repeat
            println("Sprite $i ${def.sprites?.size}")
            val sprites = def.sprites ?: return@repeat
            for ((index, sprite) in sprites.withIndex()) {
                if (sprite.width > 0 && sprite.height > 0) {
                    ImageIO.write(sprite.toBufferedImage(), "png", File("./sprites/${i}_${index}.png"))
                }
            }
        }
    }

}