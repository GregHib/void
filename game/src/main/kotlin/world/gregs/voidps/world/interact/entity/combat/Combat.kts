package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.interact.ItemOnNPC
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearWatch
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.mode.combat.CombatReached
import world.gregs.voidps.engine.entity.character.mode.combat.CombatStop
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.world.interact.entity.death.Death

on<NPCOption>({ approach && option == "Attack" }) { character: Character ->
    if (character.attackRange != 1) {
        character.approachRange(character.attackRange, update = false)
    } else {
        character.approachRange(null, update = true)
    }
    combatInteraction(player, target)
}

on<PlayerOption>({ approach && option == "Attack" }) { character: Character ->
    if (character.attackRange != 1) {
        character.approachRange(character.attackRange, update = false)
    } else {
        character.approachRange(null, update = true)
    }
    combatInteraction(player, target)
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

on<CombatStop> { character: Character ->
    if (target.dead) {
        character["face_entity"] = target
    } else {
        character.clearWatch()
    }
    character.target = null
}

on<CombatSwing> { character: Character ->
    target.start("under_attack", 16)
    if (target.inSingleCombat) {
        target.attackers.clear()
    }
    target.attackers.add(character)
}

on<CombatHit>({ source != it && it.retaliates }) { character: Character ->
    if (character.levels.get(Skill.Constitution) <= 0 || character.underAttack && character.target == source) {
        return@on
    }
    combat(character, source)
}

on<Death> { character: Character ->
    for (attacker in character.attackers) {
        if (attacker.target == character) {
            attacker.stop("under_attack")
        }
    }
}

/**
 * When triggered via [Interact] replace the Interaction with [CombatInteraction]
 * to allow movement & [Interact] to complete and start [combat] on the same tick
 * After [Interact] is complete switch to using [CombatMovement]
 */
fun combatInteraction(character: Character, target: Character) {
    val interact = character.mode as Interact
    interact.updateInteraction(CombatInteraction(character, target))
}

on<CombatInteraction> { character: Character ->
    combat(character, target)
}

/**
 * [CombatReached] is emitted by [CombatMovement] every tick the [Character] is within range of the target
 */
on<CombatReached> { character: Character ->
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
    if (character.target == null || !canAttack(character, target)) {
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