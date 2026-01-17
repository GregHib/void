package content.entity.combat

import content.entity.combat.hit.Damage
import content.entity.combat.hit.Hit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCLevels
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class CombatDebug : Script {

    init {
        modCommand(
            "maxhit",
            stringArg("npc-id", "npc to hit against", optional = true),
            stringArg("spell-id", "magic spell to use", optional = true),
            desc = "Calculate your max hit against an npc",
            handler = ::maxHit,
        )
    }

    fun maxHit(player: Player, args: List<String>) {
        val debug = player["debug", false]
        player["debug"] = false
        val npcName = args.getOrNull(0) ?: "rat"
        val spell = args.getOrNull(1) ?: "wind_rush"
        val weapon = player.equipped(EquipSlot.Weapon)
        player.message("Max Hit (target=$npcName, spell=$spell)")
        val rangeMax = Damage.maximum(player, player, "range", weapon)
        val meleeMax = Damage.maximum(player, player, "melee", weapon)
        val magicMax = Damage.maximum(player, player, "magic", weapon, spell)
        player.message("Ranged: $rangeMax Melee: $meleeMax Magic: $magicMax")
        player.message("Hit Chance")
        val target = NPC(npcName, Tile.EMPTY, NPCDefinitions.get(npcName)).apply {
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
