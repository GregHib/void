package content.entity.combat

import content.entity.combat.hit.Damage
import content.entity.combat.hit.Hit
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.modCommand
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCLevels
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.event.Script
@Script
class CombatDebug {

    val npcDefinitions: NPCDefinitions by inject()
    
    init {
        modCommand("maxhit [npc-id] [spell-id]", "calculate your max hit against an npc") {
            val debug = player["debug", false]
            player["debug"] = false
            val parts = content.split(" ")
            val npcName = if (content.isBlank() || parts.isEmpty()) "rat" else parts.first()
            val spell = if (parts.size < 2) "wind_rush" else parts[1]
            val weapon = player.equipped(EquipSlot.Weapon)
            player.message("Max Hit (target=$npcName, spell=$spell)")
            val rangeMax = Damage.maximum(player, player, "range", weapon)
            val meleeMax = Damage.maximum(player, player, "melee", weapon)
            val magicMax = Damage.maximum(player, player, "magic", weapon, spell)
            player.message("Ranged: $rangeMax Melee: $meleeMax Magic: $magicMax")
            player.message("Hit Chance")
            val target = NPC(npcName, Tile.EMPTY, npcDefinitions.get(npcName)).apply {
                levels.link(this, NPCLevels(def))
                levels.clear()
            }
            val rangeChance = Hit.chance(player, target, "range", weapon)
            val meleeChance = Hit.chance(player, target, "melee", weapon)
            val magicChance = Hit.chance(player, target, "magic", weapon)
            player.message("Ranged: $rangeChance Melee: $meleeChance Magic: $magicChance")
            player["debug"] = debug
        }

    }

}
