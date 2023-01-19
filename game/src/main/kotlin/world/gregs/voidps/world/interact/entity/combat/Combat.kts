import kotlinx.coroutines.CancellableContinuation
import org.rsmod.game.pathfinder.PathFinder
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNpcClick
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.event.Death
import world.gregs.voidps.engine.entity.character.event.Moved
import world.gregs.voidps.engine.entity.character.event.Moving
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.move.moving
import world.gregs.voidps.engine.entity.character.move.withinDistance
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.cantReach
import world.gregs.voidps.engine.entity.character.player.event.PlayerClick
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.world.interact.entity.combat.*

on<NPCClick>({ option == "Attack" }) { player: Player ->
    cancel()
    player.closeDialogue()
    player.attack(npc, firstHit = {
        player.clear("spell")
    })
}

on<PlayerClick>({ option == "Attack" }) { player: Player ->
    cancel()
    player.closeDialogue()
    player.attack(target, firstHit = {
        player.clear("spell")
    })
}

on<InterfaceOnNpcClick>({ id.endsWith("_spellbook") && canAttack(it, npc) }) { player: Player ->
    cancel()
    if (player.action.type == ActionType.Combat && player.getOrNull<NPC>("target") == npc) {
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
    target.start("in_combat", 16, restart = true)
    if (target.inSingleCombat) {
        target.attackers.clear()
    }
    target.attackers.add(character)
}

on<CombatHit>({ source != it && (it is Player && it.getVar("auto_retaliate", false) || (it is NPC && it.def["retaliates", true])) }) { character: Character ->
    if (character.levels.get(Skill.Constitution) <= 0 || character.action.type == ActionType.Combat && character.get<Character>("target") == source) {
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
        if (attacker.action.type == ActionType.Combat && attacker.target == character) {
            attacker.stop("in_combat")
            attacker.action.cancel(ActionType.Combat)
        }
    }
}

on<Moving> { character: Character ->
    for (attacker in character.attackers) {
        if (!attackable(attacker, character)) {
            attacker.mode = EmptyMode
            path(attacker, attacker.target ?: return@on)
        }
    }
}

on<VariableSet>({ key == "attack_style" && it.target != null && !attackable(it, it.target) && it.movement.path != Path.EMPTY }) { character: Character ->
    character.mode = EmptyMode
    path(character, character.target ?: return@on)
}

on<AttackDistance>({ it.target != null && !attackable(it, it.target) && it.movement.path != Path.EMPTY }) { character: Character ->
    character.mode = EmptyMode
    path(character, character.target ?: return@on)
}

on<Moved>({ attackable(it, it.target) }) { character: Character ->
    character.mode = EmptyMode
    path(character, character.target ?: return@on)
}

val pf = PathFinder(flags = world.gregs.voidps.engine.utility.get<Collisions>(), useRouteBlockerFlags = true)

fun Character.attack(target: Character, start: () -> Unit = {}, firstHit: () -> Unit = {}) {
    val source = this
    if (hasEffect("dead")) {
        return
    }
    action(ActionType.Combat) {
        source["target"] = target
        remove<CancellableContinuation<Int>>("combat_job")?.cancel()
        watch(target)
        source["first_swing"] = true
        start.invoke()

        val delay = source.remaining("skilling_delay")
        if (delay > 0 && (source.fightStyle == "range" || source.fightStyle == "magic")) {
            delay(delay.toInt())
        }
        try {
            while (isActive && (source is NPC || source is Player && source.awaitDialogues())) {
                if (!canAttack(source, target)) {
                    break
                }
                if (!attackable(source, target)) {
                    if (movement.path.state == Path.State.Complete) {
                        path(source, target)
                    } else if (source is Player && !source.moving /*&& source.cantReach()*/) {
                        source.cantReach()
                        break
                    }
                    delay()
                    continue
                }
                if (swing(this@attack, target, firstHit)) {
                    break
                }
            }
        } finally {
            watch(null)
            clear("target")
            clear("first_swing")
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
    source.start("skilling_delay", nextDelay, quiet = true)
    repeat(nextDelay) {
        if (!source.hasEffect("skilling_delay")) {
            return false
        }
        delay()
    }
    return false
}

fun Character.attackDistance(): Int {
    return (attackRange + if (attackStyle == "long_range") 2 else 0).coerceAtMost(10)
}

fun attackable(source: Character, target: Character?): Boolean {
    if (target == null || source.hasEffect("skilling_delay") && source.fightStyle == "melee") {
        return false
    }
    val distance = source.attackDistance()
    return !source.under(target) && if (distance == 1) {
        (withinDistance(source.tile, source.size, target.tile, target.size, distance, walls = true, ignore = false) && target.interactTarget.reached(source.tile, source.size))
    } else {
        (withinDistance(source.tile, source.size, target.interactTarget, distance, walls = false, ignore = false) || target.reached(source.tile, source.size))
    }
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