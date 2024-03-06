package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearWatch
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.combat.*
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.onEvent
import world.gregs.voidps.world.interact.entity.death.characterDeath
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

/**
 * When triggered via [Interact] replace the Interaction with [CombatInteraction]
 * to allow movement & [Interact] to complete and start [combat] on the same tick
 * After [Interact] is complete switch to using [CombatMovement]
 */
onEvent<CombatInteraction> {
    combat(character, target)
}

/**
 * [CombatReached] is emitted by [CombatMovement] every tick the [Character] is within range of the target
 */
onEvent<Character, CombatReached> { character ->
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
    val prepare = CombatPrepare(target)
    character.emit(prepare)
    if (prepare.cancelled) {
        character.mode = EmptyMode
        return
    }
    val swing = CombatSwing(target)
    if (character["debug", false] || target["debug", false]) {
        val player = if (character["debug", false] && character is Player) character else target as Player
        player.message("---- Swing (${character.identifier}) -> (${target.identifier}) -----")
    }
    if (!target.hasClock("under_attack")) {
        character.emit(CombatStart(target))
    }
    target.start("under_attack", 16)
    character.emit(swing)
    (character as? Player)?.specialAttack = false
    var nextDelay = character.attackSpeed
    if (character.hasClock("miasmic") && (character.fightStyle == "range" || character.fightStyle == "melee")) {
        nextDelay *= 2
    }
    character.start("hit_delay", nextDelay)
}

characterCombatStart { character ->
    if (target.inSingleCombat) {
        target.attackers.clear()
    }
    target.attackers.add(character)
}

characterCombatStop { character ->
    if (target.dead) {
        character["face_entity"] = target
    } else {
        character.clearWatch()
    }
    character.target = null
}

characterDeath { character ->
    character.stop("under_attack")
    for (attacker in character.attackers) {
        if (attacker.target == character) {
            attacker.stop("under_attack")
        }
    }
}