package content.quest.member.fairy_tale_part_2.fairy_ring

import content.bot.behaviour.navigation.Graph.Companion.readTile
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Tile

class FairyRingCodes {

    lateinit var codes: Map<String, FairyRingCode>
        private set

    fun load(path: String): FairyRingCodes {
        timedLoad("fairy ring code") {
            val codes = Object2ObjectOpenHashMap<String, FairyRingCode>(40)
            Config.fileReader(path) {
                while (nextSection()) {
                    val stringId = section()
                    var name = ""
                    var tile = Tile.EMPTY
                    while (nextPair()) {
                        when (val key = key()) {
                            "tile" -> tile = readTile()
                            "name" -> name = string()
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    codes[stringId.lowercase()] = FairyRingCode(stringId, name, tile)
                }
            }
            this.codes = codes
            codes.size
        }
        return this
    }

    data class FairyRingCode(val id: String, val name: String, val tile: Tile)
}
