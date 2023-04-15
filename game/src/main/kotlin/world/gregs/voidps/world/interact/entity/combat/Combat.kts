package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNPC
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearWatch
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.mode.combat.CombatReached
import world.gregs.voidps.engine.entity.character.mode.combat.CombatStop
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.world.interact.entity.death.Death

on<NPCOption>({ approach && option == "Attack" }) { player: Player ->
    player.approachRange(player.attackRange, update = false)
    combat(player, npc)
}

on<PlayerOption>({ approach && option == "Attack" }) { player: Player ->
    player.approachRange(player.attackRange, update = false)
    combat(player, target)
}

on<InterfaceOnNPC>({ approach && id.endsWith("_spellbook") }, Priority.HIGH) { player: Player ->
    player.approachRange(8, update = false)
    player.spell = component
    player["attack_speed"] = 5
    player.attackRange = 8
    combat(player, npc, 8)
    cancel()
}

on<CombatReached> { character: Character ->
    combat(character, target)
}

fun combat(character: Character, target: Character, attackRange: Int = character.attackRange) {
    val movement: CombatMovement = if (character.mode is CombatMovement) {
        (character.mode as CombatMovement)
    } else {
        CombatMovement(character, target)
    }
    character.mode = movement
    if (character.target != target) {
        character.target = target
    }
    if (character is Player && character.dialogue != null) {
        return
    }
    if (character.target == null || !canAttack(character, target)) {
        character.mode = EmptyMode
        return
    }
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

on<CombatSwing>(priority = Priority.HIGHEST) { character: Character ->
    character.face(target)
}

on<CombatStop> { character: Character ->
    character.clearWatch()
    character.target = null
}

on<CombatSwing> { character: Character ->
    target.start("in_combat", 16)
    if (target.inSingleCombat) {
        target.attackers.clear()
    }
    target.attackers.add(character)
}

on<CombatHit>({ source != it && it.retaliates }) { character: Character ->
    if (character.levels.get(Skill.Constitution) <= 0 || character.hasClock("in_combat") && character.target == source) {
        return@on
    }
    combat(character, source)
}

on<Death> { character: Character ->
    for (attacker in character.attackers) {
        if (attacker.target == character) {
            attacker.stop("in_combat")
        }
    }
}
