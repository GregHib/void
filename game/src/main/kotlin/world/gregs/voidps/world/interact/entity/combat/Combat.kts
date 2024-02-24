package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearWatch
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.mode.combat.CombatReached
import world.gregs.voidps.engine.entity.character.mode.combat.CombatStop
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.onCharacter
import world.gregs.voidps.world.interact.entity.death.characterDeath

/**
 * When triggered via [Interact] replace the Interaction with [CombatInteraction]
 * to allow movement & [Interact] to complete and start [combat] on the same tick
 * After [Interact] is complete switch to using [CombatMovement]
 */
onCharacter<CombatInteraction> { character ->
    combat(character, target)
}

/**
 * [CombatReached] is emitted by [CombatMovement] every tick the [Character] is within range of the target
 */
onCharacter<CombatReached> { character ->
    combat(character, target)
}

fun combat(character: Character, target: Character) {
    if (character.mode !is CombatMovement || character.target != target) {
        character.mode = CombatMovement(character, target)
        character.target = target
    }
    val movement = character.mode as CombatMovement
    if (character is Player && character.dialogue != null) {
        return
    }
    if (character.target == null || !Target.attackable(character, target)) {
        character.mode = EmptyMode
        return
    }
    val attackRange = character.attackRange
    if (!movement.arrived(if (attackRange == 1) -1 else attackRange)) {
        return
    }
    if (character.hasClock("hit_delay")) {
        return
    }
    val swing = CombatSwing(target)
    character.events.emit(swing)
    val nextDelay = swing.delay
    if (nextDelay == null || nextDelay < 0) {
        character.mode = EmptyMode
        return
    }
    character.start("hit_delay", nextDelay)
}

onCharacter<CombatStop> { character ->
    if (target.dead) {
        character["face_entity"] = target
    } else {
        character.clearWatch()
    }
    character.target = null
}

onCharacter<CombatSwing> { character ->
    target.start("under_attack", 16)
    if (target.inSingleCombat) {
        target.attackers.clear()
    }
    target.attackers.add(character)
}

characterDeath { character ->
    character.stop("under_attack")
    for (attacker in character.attackers) {
        if (attacker.target == character) {
            attacker.stop("under_attack")
        }
    }
}