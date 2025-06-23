package content.entity.npc.combat

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.hunt.huntNPC
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.inject

val areas: AreaDefinitions by inject()

huntPlayer(mode = "aggressive*") { npc ->
    if (!Settings["world.npcs.aggression", true] || attacking(npc, target)) {
        return@huntPlayer
    }
    if (Settings["world.npcs.safeZone", false] && npc.tile in areas["lumbridge"]) {
        return@huntPlayer
    }
    npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
}

huntPlayer(mode = "cowardly") { npc ->
    if (!Settings["world.npcs.aggression", true] || attacking(npc, target)) {
        return@huntPlayer
    }
    if (Settings["world.npcs.safeZone", false] && npc.tile in areas["lumbridge"]) {
        return@huntPlayer
    }
    npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
}

huntNPC(mode = "aggressive*") { npc ->
    if (attacking(npc, target)) {
        return@huntNPC
    }
    npc.mode = Interact(npc, target, NPCOption(npc, target, target.def, "Attack"))
}

huntNPC(mode = "cowardly") { npc ->
    if (attacking(npc, target)) {
        return@huntNPC
    }
    npc.mode = Interact(npc, target, NPCOption(npc, target, target.def, "Attack"))
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
