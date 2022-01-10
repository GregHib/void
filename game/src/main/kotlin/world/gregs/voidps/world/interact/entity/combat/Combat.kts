import kotlinx.coroutines.CancellableContinuation
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
import world.gregs.voidps.engine.entity.character.Death
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.Moving
import world.gregs.voidps.engine.entity.character.move.cantReach
import world.gregs.voidps.engine.entity.character.move.moving
import world.gregs.voidps.engine.entity.character.move.withinDistance
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.cantReach
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.PathType
import world.gregs.voidps.world.interact.entity.combat.*

on<NPCClick>({ !cancel && option == "Attack" }) { player: Player ->
    cancel = true
    player.closeDialogue()
    player.attack(npc, firstHit = {
        player.clear("spell")
    })
}

on<InterfaceOnNpcClick>({ id.endsWith("_spellbook") }) { player: Player ->
    cancel = true
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

on<CombatHit>({ it is Player && it.getVar("auto_retaliate", false) || (it is NPC && it.def["retaliates", true]) }) { character: Character ->
    if (character.levels.get(Skill.Constitution) <= 0 || character.action.type == ActionType.Combat && character.get<Character>("target") == source) {
        return@on
    }
    character.attack(source)
}

fun Character.attack(target: Character, start: () -> Unit = {}, firstHit: () -> Unit = {}) {
    val source = this
    if (action.type == ActionType.Dying) {
        return
    }
    action(ActionType.Combat) {
        source["target"] = target
        remove<CancellableContinuation<Int>>("combat_job")?.cancel()
        watch(target)
        source["first_swing"] = true
        start.invoke()

        val deathHandler = target.events.on<Character, Death> {
            source.stop("in_combat")
            cancel(ActionType.Combat)
        }
        val targetHandler = target.events.on<Character, Moving>({ !attackable(source, it) }) {
            source.movement.path.recalculate()
        }
        val styleHandler = events.on<Character, VariableSet>({ key == "attack_style" && !attackable(it, target) }) {
            source.movement.path.recalculate()
        }
        val distanceHandler = events.on<Character, AttackDistance>({ !attackable(it, target) }) {
            source.movement.path.recalculate()
        }
        val moveHandler = events.on<Character, Moved>({ attackable(it, target) }) {
            it.movement.path.steps.clear()
            it.movement.path.result = PathResult.Success(it.tile)
        }
        val delay = source.remaining("skilling_delay")
        if (delay > 0) {
            delay(delay.toInt())
        }
        try {
            while (isActive && (source is NPC || source is Player && source.awaitDialogues())) {
                if (!canAttack(source, target)) {
                    break
                }
                if (!attackable(source, target)) {
                    if (!source["combat_path_set", false]) {
                        source["combat_path_set"] = true
                        movement.set(target.interactTarget, if (source is Player) PathType.Smart else PathType.Dumb)
                    } else if (source is Player && !source.moving && source.cantReach(movement.path)) {
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
            clear("combat_path_set")
            clear("first_swing")
            events.remove(distanceHandler)
            events.remove(styleHandler)
            events.remove(moveHandler)
            target.events.remove(deathHandler)
            target.events.remove(targetHandler)
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

fun attackable(source: Character, target: Character): Boolean {
    val distance = source.attackDistance()
    return !source.under(target) && (withinDistance(source.tile, source.size, target.interactTarget, distance, distance == 1, false) || target.interactTarget.reached(source.tile, source.size))
}