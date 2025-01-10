package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.AnimationDecoderFull

object AnimationDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("storage.cache.path"))
        val decoder = AnimationDecoderFull().load(cache)
        val match = decoder[10289].frames
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.frames.contentEquals(match)) {
                println("$i")
            }
        }
        println(convertOsrs(589))
    }

    fun convertOsrs(id: Int): String {
        return when(id) {
            554, 555, 556, 557 -> "quiz"
            562, 563, 564, 565 -> "roll_eyes"
            567, 568, 569, 570 -> "happy"
            571, 572, 573, 574 -> "surprised"
            575, 576, 577, 578 -> "uncertain"
            588, 589, 590, 591 -> "talk"
            592, 593, 594, 595 -> "shifty"
            596, 597, 598, 599 -> "afraid"
            600, 601, 602, 603 -> "drunk"
            605, 606, 607, 608 -> "chuckle"
            609 -> "evil_laugh"
            610, 611, 612, 613 -> "upset"
            614, 615, 616, 617 -> "angry"
            else -> id.toString()
        }
    }
}