package content.entity.npc.combat

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

class Aggression : Script {

    init {
        huntPlayer(mode = "aggressive", handler = ::playerHandler)

        huntPlayer(mode = "aggressive_intolerant", handler = ::playerHandler)

        huntPlayer(mode = "cowardly") { target ->
            if (!Settings["world.npcs.aggression", true] || attacking(this, target)) {
                return@huntPlayer
            }
            if (Settings["world.npcs.safeZone", false] && tile in Areas["lumbridge"]) {
                return@huntPlayer
            }
            interactPlayer(target, "Attack")
        }

        huntNPC(mode = "aggressive", handler = ::npcHandler)
        huntNPC(mode = "aggressive_intolerant", handler = ::npcHandler)

        huntNPC(mode = "cowardly") { target ->
            if (attacking(this, target)) {
                return@huntNPC
            }
            interactNpc(target, "Attack")
        }
    }

    fun playerHandler(npc: NPC, target: Player) {
        if (!Settings["world.npcs.aggression", true] || attacking(npc, target)) {
            return
        }
        if (Settings["world.npcs.safeZone", false] && npc.tile in Areas["lumbridge"]) {
            return
        }
        npc.interactPlayer(target, "Attack")
    }

    fun npcHandler(npc: NPC, target: NPC) {
        if (!attacking(npc, target)) {
            npc.interactNpc(target, "Attack")
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
