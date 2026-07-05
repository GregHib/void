package content.skill.hunter

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.type.Tile

object Traps {
    fun max(level: Int, max: Int) = (1 + level / 20).coerceAtMost(max)

    fun chance(npc: NPC, creature: RowDefinition): IntRange {
        val chance = creature.intRange("chance")
        var add = 0
        if (npc.contains("bait")) {
            add += 7 // 3%
        }
        if (npc["smoked", false]) {
            add += 5 // 2%
        }
        if (add == 0) {
            return chance
        }
        return (chance.first + add)..(chance.last + add)
    }

    fun smoke(player: Player, trap: String, tile: Tile) {
        val id = Tables.npc("traps.$trap.npc")
        val npc = NPCs.find(tile, id)
        if (npc["owner", ""] != player.accountName) {
            player.message("This isn't your trap.")
            return
        }
        if (npc["smoked", false]) {
            player.message("You've already smoked this trap.")
            return
        }
        player.anim("lay_trap_small")
        areaSound("hunting_smoke2", tile = tile, radius = 5)
        npc["smoked"] = true
        player.message("You use the smoke from the torch to remove your scent from the trap.", type = ChatType.Filter)
    }
}
