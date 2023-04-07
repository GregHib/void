package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNpcClick
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearWatch
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.CombatAttempt
import world.gregs.voidps.engine.entity.character.mode.CombatFollow
import world.gregs.voidps.engine.entity.character.mode.CombatStop
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.map.Overlap
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.world.interact.entity.death.Death

on<NPCOption>({ approach && option == "Attack" }) { player: Player ->
    player.closeDialogue()
    player.mode = CombatFollow(player, npc)
}

on<CombatAttempt>({ swingCount == 0 }, Priority.HIGHEST) { character: Character ->
    if (character.dead) {
        cancel()
        character.mode = EmptyMode
        return@on
    }
    character.clear("spell")
    character["target"] = target
    character.watch(target)
}

on<CombatAttempt>({ swingCount == 0 && it.contains("queued_spell") }) { player: Player ->
    player.spell = player.remove("queued_spell") ?: return@on
    player["attack_speed"] = 5
}

on<CombatAttempt>({ it.hasClock("hit_delay") }, Priority.HIGHEST) { character: Character ->
    cancel()
}

on<CombatAttempt>(priority = Priority.LOWEST) { character: Character ->
    val swing = CombatSwing(target)
    character.events.emit(swing)
    val nextDelay = swing.delay
    if (nextDelay == null || nextDelay < 0) {
        character.mode = EmptyMode
        return@on
    }
    character.start("hit_delay", nextDelay)
}

on<CombatSwing>(priority = Priority.HIGHEST) { character: Character ->
    if (character is Player && character.dialogue != null) {
        delay = 1
        return@on
    }
    if (character.target == null) {
        character.mode = EmptyMode
        delay = -1
        return@on
    }
    if (!canAttack(character, target)) {
        character.mode = EmptyMode
        delay = -1
        return@on
    }
    character.face(target)
}

on<CombatStop> { character: Character ->
    character.clearWatch()
    character.clear("target")
}

on<PlayerOption>({ approach && option == "Attack" }) { player: Player ->
    player.closeDialogue()
    player.mode = CombatFollow(player, target)
}

on<InterfaceOnNpcClick>({ id.endsWith("_spellbook") && canAttack(it, npc) }) { player: Player ->
    cancel()
    if (player.hasClock("in_combat") && player.getOrNull<NPC>("target") == npc) {
        player.spell = component
        player.attackRange = 8
        player["attack_speed"] = 5
    } else {
        player.closeDialogue()
        player["queued_spell"] = component
        player.mode = CombatFollow(player, npc, start = {
            player.attackRange = 8
        })
    }
}

on<CombatSwing> { character: Character ->
    target.start("in_combat", 16)
    if (target.inSingleCombat) {
        target.attackers.clear()
    }
    target.attackers.add(character)
}

on<CombatHit>({ source != it && (it is Player && it["auto_retaliate", false] || (it is NPC && it.def["retaliates", true])) }) { character: Character ->
    if (character.levels.get(Skill.Constitution) <= 0 || character.hasClock("in_combat") && character.target == source) {
        return@on
    }
    character.mode = CombatFollow(character, source)
}

var Character.target: Character?
    get() = getOrNull("target")
    set(value) {
        if (value != null) {
            set("target", value)
        } else {
            clear("target")
        }
    }

on<Death> { character: Character ->
    for (attacker in character.attackers) {
        if (attacker.target == character) {
            attacker.stop("in_combat")
        }
    }
}

on<VariableSet>({ key == "attack_style_index" && it.target != null && !attackable(it, it.target) && it.mode is Movement && it.mode !is CombatFollow }) { character: Character ->
    character.mode = EmptyMode
}

on<AttackDistance>({ it.target != null && !attackable(it, it.target) && it.mode is Movement && it.mode !is CombatFollow }) { character: Character ->
    character.mode = EmptyMode
}

on<Moved>({ attackable(it, it.target) }) { character: Character ->
    character.mode = EmptyMode
}

fun Character.attackDistance(): Int {
    return (attackRange + if (attackStyle == "long_range") 2 else 0).coerceAtMost(10)
}

fun attackable(source: Character, target: Character?): Boolean {
    if (target == null || source.fightStyle == "melee") {
        return false
    }
    val distance = source.attackDistance()
    return !source.under(target) && (withinDistance(source.tile, source.size, target.tile, target.size, distance, distance == 1, false) /*|| target.reached(source.tile, source.size)*/)
}

fun withinDistance(tile: Tile, size: Size, target: Tile, targetSize: Size, distance: Int, walls: Boolean = false, ignore: Boolean = true): Boolean {
    if (Overlap.isUnder(tile, size, target, targetSize)) {
        return false
    }
    return distance > 0 && tile.distanceTo(target, targetSize) <= distance && tile.withinSight(Distance.getNearest(target, targetSize, tile), walls = walls, ignore = ignore)
}