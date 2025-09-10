package content.entity.npc.combat

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.hunt.HuntNPC
import world.gregs.voidps.engine.entity.character.npc.hunt.HuntPlayer
import world.gregs.voidps.engine.entity.character.npc.hunt.huntNPC
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.event.Script
@Script
class Aggression {

    val areas: AreaDefinitions by inject()
    
    val playerHandler: suspend HuntPlayer.(npc: NPC) -> Unit = huntPlayer@{ npc ->
        if (!Settings["world.npcs.aggression", true] || attacking(npc, target)) {
            return@huntPlayer
        }
        if (Settings["world.npcs.safeZone", false] && npc.tile in areas["lumbridge"]) {
            return@huntPlayer
        }
        npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
    }
    val npcHandler: suspend HuntNPC.(npc: NPC) -> Unit = { npc ->
        if (!attacking(npc, target)) {
            npc.mode = Interact(npc, target, NPCOption(npc, target, target.def, "Attack"))
        }
    }

    init {
        huntPlayer(mode = "aggressive", handler = playerHandler)

        huntPlayer(mode = "aggressive_intolerant", handler = playerHandler)

        huntPlayer(mode = "cowardly") { npc ->
            if (!Settings["world.npcs.aggression", true] || attacking(npc, target)) {
                return@huntPlayer
            }
            if (Settings["world.npcs.safeZone", false] && npc.tile in areas["lumbridge"]) {
                return@huntPlayer
            }
            npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
        }

        huntNPC(mode = "aggressive", handler = npcHandler)

        huntNPC(mode = "aggressive_intolerant", handler = npcHandler)

        huntNPC(mode = "cowardly") { npc ->
            if (attacking(npc, target)) {
                return@huntNPC
            }
            npc.mode = Interact(npc, target, NPCOption(npc, target, target.def, "Attack"))
        }

    }

    fun attacking(npc: NPC, target: Character): Boolean {
        val current = npc.mode
        if (current is Interact && current.target == target) {
            return true
        } else if (current is CombatMovement && current.target == target) {
            return true
        }
        return false
    }
}
