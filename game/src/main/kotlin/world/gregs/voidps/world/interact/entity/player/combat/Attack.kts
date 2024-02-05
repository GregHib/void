package world.gregs.voidps.world.interact.entity.player.combat

import world.gregs.voidps.engine.client.ui.interact.itemOnNPCApproach
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.npcApproach
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.playerApproach
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.world.interact.entity.combat.CombatInteraction
import world.gregs.voidps.world.interact.entity.combat.attackRange
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell

npcApproach({ option == "Attack" }) { character: Character ->
    if (character.attackRange != 1) {
        character.approachRange(character.attackRange, update = false)
    } else {
        character.approachRange(null, update = true)
    }
    combatInteraction(character, target)
}

playerApproach({ option == "Attack" }) { character: Character ->
    if (character.attackRange != 1) {
        character.approachRange(character.attackRange, update = false)
    } else {
        character.approachRange(null, update = true)
    }
    combatInteraction(character, target)
}

itemOnNPCApproach({ id.endsWith("_spellbook") }, Priority.HIGH) { player: Player ->
    player.approachRange(8, update = false)
    player.spell = component
    player["attack_speed"] = 5
    player["one_time"] = true
    player.attackRange = 8
    player.face(target)
    combatInteraction(player, target)
    cancel()
}

combatSwing({ it.contains("one_time") }) { player: Player ->
    player.mode = EmptyMode
    player.clear("one_time")
}

/**
 * Switch out the current Interaction with [CombatInteraction] to allow hits this tick
 */
fun combatInteraction(character: Character, target: Character) {
    val interact = character.mode as? Interact ?: return
    interact.updateInteraction(CombatInteraction(character, target))
}