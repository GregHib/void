package content.quest.member.fairy_tale_part_2.fairy_ring

import content.bot.behaviour.navigation.NavigationGraph.Companion.readTile
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.type.Tile

class FairyRingCodes {

    lateinit var codes: Map<String, FairyRingCode>
        private set

    fun load(path: String): FairyRingCodes {
        val codes = Object2ObjectOpenHashMap<String, FairyRingCode>(40)
        Config.fileReader(path) {
            while (nextSection()) {
                val stringId = section()
                var name = ""
                var tile = Tile.EMPTY
                var quest = ""
                while (nextPair()) {
                    when (val key = key()) {
                        "tile" -> tile = readTile()
                        "name" -> name = string()
                        "quest" -> quest = string()
                        else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                    }
                }
                codes[stringId.lowercase()] = FairyRingCode(stringId, name, tile, quest)
            }
        }
        this.codes = codes
        return this
    }

    data class FairyRingCode(val id: String, val name: String, val tile: Tile, val quest: String = "")

    fun copyFrom(other: FairyRingCodes) {
        codes = other.codes
    }
}

object FairyRingCodesLoader : Loader<FairyRingCodes> {
    val instance = FairyRingCodes()
    override val name = "fairy_ring_codes"
    override val file = listOf("definitions.fairyCodes")

    override fun load(context: LoadContext): FairyRingCodes = FairyRingCodes().load(context.paths(this).single())

    override fun install(state: FairyRingCodes) = instance.copyFrom(state)
}
