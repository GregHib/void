package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import org.koin.fileProperties
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule

object AnimationDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = AnimationDecoder(koin.get())
        loop@ for (i in 0 until decoder.last) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.aBoolean691) {
                println("$i $def")
            }
        }
        println(convertOsrs(589))
    }

    fun convertOsrs(id: Int): String {
        return when(id) {
            554, 555, 556, 557 -> "unsure"
            562, 563, 564, 565 -> "roll_eyes"
            567, 568, 569, 570 -> "cheerful"
            571, 572, 573, 574 -> "surprised"
            575, 576, 577, 578 -> "uncertain"
            588, 589, 590, 591 -> "talk"
            592, 593, 594, 595 -> "suspicious"
            596, 597, 598, 599 -> "afraid"
            600, 601, 602, 603 -> "drunk"
            605, 606, 607, 608 -> "chuckle"
            609 -> "evil_laugh"
            610, 611, 612, 613 -> "upset"
            614, 615, 616, 617 -> "furious"
            else -> id.toString()
        }
    }
}