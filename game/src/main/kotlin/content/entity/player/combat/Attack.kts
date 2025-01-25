package content.entity.player.combat

import world.gregs.voidps.engine.client.ui.interact.itemOnNPCApproach
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.characterApproachNPC
import world.gregs.voidps.engine.entity.character.player.characterApproachPlayer
import world.gregs.voidps.world.interact.entity.combat.CombatInteraction
import world.gregs.voidps.world.interact.entity.combat.attackRange
import world.gregs.voidps.world.interact.entity.combat.combatPrepare
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell

characterApproachNPC("Attack") {
    if (character.attackRange != 1) {
        approachRange(character.attackRange, update = false)
    } else {
        approachRange(null, update = true)
    }
    combatInteraction(character, target)
}

characterApproachPlayer("Attack") {
    if (character.attackRange != 1) {
        approachRange(character.attackRange, update = false)
    } else {
        approachRange(null, update = true)
    }
    combatInteraction(character, target)
}

itemOnNPCApproach(id = "*_spellbook") {
    approachRange(8, update = false)
    player.spell = component
    player["attack_speed"] = 5
    player["one_time"] = true
    player.attackRange = 8
    player.face(target)
    combatInteraction(player, target)
    cancel()
}

combatPrepare {  player ->
    if (player.contains("one_time")) {
        player.mode = EmptyMode
        player.clear("one_time")
    }
}

/**
 * Switch out the current Interaction with [CombatInteraction] to allow hits this tick
 */
fun combatInteraction(character: Character, target: Character) {
    val interact = character.mode as? Interact ?: return
    interact.updateInteraction(CombatInteraction(character, target))
}