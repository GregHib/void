package world.gregs.voidps.world.interact.entity.player.combat

import world.gregs.voidps.engine.client.ui.interact.ItemOnNPC
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.world.interact.entity.combat.CombatInteraction
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.attackRange
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell

on<NPCOption>({ approach && option == "Attack" }) { character: Character ->
    if (character.attackRange != 1) {
        character.approachRange(character.attackRange, update = false)
    } else {
        character.approachRange(null, update = true)
    }
    combatInteraction(character, target)
}

on<PlayerOption>({ approach && option == "Attack" }) { character: Character ->
    if (character.attackRange != 1) {
        character.approachRange(character.attackRange, update = false)
    } else {
        character.approachRange(null, update = true)
    }
    combatInteraction(character, target)
}

on<ItemOnNPC>({ approach && id.endsWith("_spellbook") }, Priority.HIGH) { player: Player ->
    player.approachRange(8, update = false)
    player.spell = component
    player["attack_speed"] = 5
    player["one_time"] = true
    player.attackRange = 8
    player.face(target)
    combatInteraction(player, target)
    cancel()
}

on<CombatSwing>({ it.contains("one_time") }) { player: Player ->
    player.mode = EmptyMode
    player.clear("one_time")
}

/**
 * Switch out the current Interaction with [CombatInteraction] to allow hits this tick
 */
fun combatInteraction(character: Character, target: Character) {
    val interact = character.mode as Interact
    interact.updateInteraction(CombatInteraction(character, target))
}