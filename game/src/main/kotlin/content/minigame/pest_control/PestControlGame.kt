package content.minigame.pest_control

import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Region

/**
 * A single game of pest control on its own copy of the island
 */
class PestControlGame(
    val id: Int,
    val lander: RowDefinition,
    val instance: Region,
    val offset: Delta,
    val shieldOrder: List<String>,
) {
    val players = mutableListOf<Player>()
    val portals = mutableMapOf<String, NPC>()
    val monsters = mutableListOf<NPC>()
    var knight: NPC? = null
    var squire: NPC? = null
    var ticks = 0
    var over = false
}
