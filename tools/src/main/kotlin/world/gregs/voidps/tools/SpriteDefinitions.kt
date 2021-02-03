package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.cache.definition.decoder.SpriteDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import java.io.File
import javax.imageio.ImageIO

object SpriteDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = SpriteDecoder(koin.get())
        println(decoder.size)
        val cache: Cache = koin.get()
        println(cache.lastArchiveId(8))
        println(cache.archiveCount(8, cache.lastArchiveId(8)))
        File("./sprites/").mkdir()
        repeat(decoder.size) { i ->
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