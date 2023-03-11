package world.gregs.voidps.world.interact.entity.combat

import kotlinx.coroutines.CancellableContinuation
import org.rsmod.game.pathfinder.PathFinder
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNpcClick
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearWatch
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.character.player.chat.cantReach
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.map.Overlap
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.queue.Action
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.suspend.awaitDialogues
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.world.interact.entity.death.Death

on<NPCOption>({ approach && option == "Attack" }) { player: Player ->
    player.closeDialogue()
    player.attack(npc, firstHit = {
        player.clear("spell")
    })
}

on<PlayerOption>({ approach && option == "Attack" }) { player: Player ->
    player.closeDialogue()
    player.attack(target, firstHit = {
        player.clear("spell")
    })
}

on<InterfaceOnNpcClick>({ id.endsWith("_spellbook") && canAttack(it, npc) }) { player: Player ->
    cancel()
    if (player.hasClock("in_combat") && player.getOrNull<NPC>("target") == npc) {
        player.spell = component
        player.attackRange = 8
        player["attack_speed"] = 5
    } else {
        player.attack(npc, start = {
            player.attackRange = 8
        }, firstHit = {
            player.spell = component
            player["attack_speed"] = 5
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
    if (character.levels.get(Skill.Constitution) <= 0 || character.hasClock("in_combat") && character.get<Character>("target") == source) {
        return@on
    }
    character.attack(source)
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

/*on<Moving> { character: Character ->
    for (attacker in character.attackers) {
        if (!attackable(attacker, character)) {
            attacker.mode = EmptyMode
            path(attacker, attacker.target ?: return@on)
        }
    }
}*/

on<VariableSet>({ key == "attack_style_index" && it.target != null && !attackable(it, it.target) && it.mode is Movement }) { character: Character ->
    character.mode = EmptyMode
    path(character, character.target ?: return@on)
}

on<AttackDistance>({ it.target != null && !attackable(it, it.target) && it.mode is Movement }) { character: Character ->
    character.mode = EmptyMode
    path(character, character.target ?: return@on)
}

on<Moved>({ attackable(it, it.target) }) { character: Character ->
    character.mode = EmptyMode
    path(character, character.target ?: return@on)
}

val pf = PathFinder(flags = get<Collisions>(), useRouteBlockerFlags = true)

fun Character.attack(target: Character, start: () -> Unit = {}, firstHit: () -> Unit = {}) {
    val source = this
    if (get("dead", false)) {
        return
    }
    queue {
        source["target"] = target
        remove<CancellableContinuation<Int>>("combat_job")?.cancel()
        watch(target)
        source["first_swing"] = true
        start.invoke()
        onCancel = {
            clearWatch()
            clear("target")
            clear("combat_path_set")
            clear("first_swing")
        }
        while ((source is NPC || source is Player && source.awaitDialogues())) {
            if (!canAttack(source, target)) {
                break
            }
            if (!attackable(source, target)) {
                if (!source["combat_path_set", false]) {
                    source["combat_path_set"] = true
                    path(source, target)
                } else if (source is Player /*&& !source.moving && source.cantReach()*/) {
                    source.cantReach()
                    break
                }
                pause()
                continue
            }
            if (swing(this@attack, target, firstHit)) {
                break
            }
        }
    }
}

suspend fun Action.swing(source: Character, target: Character, firstHit: () -> Unit): Boolean {
    if (source["first_swing", false]) {
        firstHit.invoke()
        source.clear("first_swing")
    }
    val swing = CombatSwing(target)
    source.face(target)
    source.events.emit(swing)
    val nextDelay = swing.delay
    if (nextDelay == null || nextDelay < 0) {
        return true
    }
    /*source.start("skilling_delay", nextDelay, quiet = true)
    repeat(nextDelay) {
        if (!source.hasEffect("skilling_delay")) {
            return false
        }
        pause()
    }*/
    return false
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

fun path(character: Character, target: Character) {
    if (character is Player) {
        val route = pf.findPath(
            srcX = character.tile.x,
            srcY = character.tile.y,
            destX = target.tile.x,
            destY = target.tile.y,
            level = character.tile.plane,
            srcSize = character.size.width,
            destWidth = target.size.width,
            destHeight = target.size.height)
//        character.movement.queueRoute(route) FIXME
    } else {
//        character.movement.queueStep(target.tile, false)
    }
}

fun Character.queue(function: suspend Action.() -> Unit) {
    if (this is NPC) {
        queue(name = "combat", block = function)
    } else if (this is Player) {
        queue(name = "combat", block = function)
    }
}
