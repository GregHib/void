package content.entity.combat

import content.area.wilderness.inSingleCombat
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.combat.*
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.characterDespawn
import world.gregs.voidps.engine.event.onEvent
import content.entity.combat.hit.characterCombatDamage
import content.entity.death.characterDeath
import content.entity.player.combat.special.specialAttack
import content.skill.melee.weapon.attackRange
import content.skill.melee.weapon.attackSpeed
import content.skill.melee.weapon.fightStyle
import content.skill.melee.weapon.weapon

/**
 * When triggered via [Interact] replace the Interaction with [CombatInteraction]
 * to allow movement & [Interact] to complete and start [combat] on the same tick
 * After [Interact] is complete switch to using [CombatMovement]
 */
onEvent<CombatInteraction<*>> {
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
    if (!movement.arrived(if (attackRange == 1 && character.weapon.def["weapon_type", ""] != "salamander") -1 else attackRange)) {
        return
    }
    if (character.hasClock("action_delay")) {
        return
    }
    val prepare = CombatPrepare(target)
    character.emit(prepare)
    if (prepare.cancelled) {
        character.mode = EmptyMode
        return
    }
    if (character["debug", false] || target["debug", false]) {
        val player = if (character["debug", false] && character is Player) character else target as Player
        player.message("---- Swing (${character.identifier}) -> (${target.identifier}) -----")
    }
    if (!target.hasClock("in_combat")) {
        character.emit(CombatStart(target))
    }
    target.start("in_combat", 8)
    val swing = CombatSwing(target)
    character.emit(swing)
    (character as? Player)?.specialAttack = false
    var nextDelay = character.attackSpeed
    if (character.hasClock("miasmic") && (character.fightStyle == "range" || character.fightStyle == "melee")) {
        nextDelay *= 2
    }
    character.start("action_delay", nextDelay)
}

characterDespawn { character ->
    for (attacker in character.attackers) {
        attacker.mode = EmptyMode
    }
}

characterCombatStart { character ->
    if (target.inSingleCombat) {
        target.attackers.clear()
        target.attacker = character
    }
    target.attackers.add(character)
    retaliate(target, character)
}

characterCombatStop { character ->
    if (target.dead) {
        character["face_entity"] = target
    } else {
        character.clearWatch()
    }
    character.target?.attackers?.remove(character)
    character.target = null
}

characterDeath { character ->
    character.stop("in_combat")
    for (attacker in character.attackers) {
        if (attacker.target == character) {
            attacker.stop("in_combat")
        }
    }
}

characterCombatDamage { character ->
    if (source == character || type == "poison" || type == "disease" || type == "healed") {
        return@characterCombatDamage
    }
    if (character.mode !is CombatMovement) {
        retaliate(character, source)
    }
}

fun retaliates(character: Character) = if (character is NPC) {
    character.def["retaliates", true]
} else {
    character["auto_retaliate", false]
}

fun retaliate(character: Character, source: Character) {
    if (character.dead || character.levels.get(Skill.Constitution) <= 0 || !retaliates(character)) {
        return
    }
    if (character is Player && character.mode != EmptyMode) {
        return
    }
    if (character is NPC && character.mode is CombatMovement && character.hasClock("in_combat")) {
        return
    }
    character.mode = CombatMovement(character, source)
    character.target = source
    val delay = character.attackSpeed / 2
    character.start("action_delay", delay)
    character.start("in_combat", delay + 8)
}