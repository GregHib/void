import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Died
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.move.cantReach
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.remaining
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy.Companion.isWithinAttackDistance
import world.gregs.voidps.network.encode.message

on<NPCClick>({ option == "Attack" }) { player: Player ->
    cancel = true
    attack(player, npc)
}

fun attack(player: Player, target: Character) {
    if (player.action.type == ActionType.Combat && player.hasEffect("skilling_delay")) {
        return
    }
    player.action(ActionType.Combat) {
        val moveHandler = target.events.on<NPC, Moved> {
            val style = player.getVar("attack_style", 0)
            withinRange(player, target, style)
        }
        val deathHandler = target.events.on<NPC, Died> {
            cancel(ActionType.Combat)
        }
        try {
            player.watch(target)
            while (isActive && player.awaitDialogues()) {
                if (player.hasEffect("skilling_delay")) {
                    delay(player.remaining("skilling_delay").toInt())
                }
                val style = player.getVar("attack_style", 0)
                if (!withinRange(player, target, style)) {
                    await<Unit>(Suspension.Movement)
                }
                if (!canAttack(player, target)) {
                    break
                }
                val nextDelay = player.attackSwing(target)
                if (nextDelay < 0) {
                    break
                }
                player.start("skilling_delay", nextDelay, quiet = true)
            }
        } finally {
            target.events.remove(moveHandler)
            target.events.remove(deathHandler)
            player.watch(null)
        }
    }
}

fun canAttack(player: Player, target: Character): Boolean {
    // PVP area, slayer requirements, in combat etc..
    return true
}

fun withinRange(player: Player, target: Character, style: Int): Boolean {
    val type = player["attack_type", "melee"]
    val longRange = type == "ranged" && style == 2
    val maxDistance = (player["attack_range", 1] + if (longRange) 2 else 0).coerceAtMost(10)
    val closeCombat = maxDistance == 1
    val strategy = CombatTargetStrategy(target, maxDistance, closeCombat)
    if (!isWithinAttackDistance(player.tile.x, player.tile.y, player.tile.plane, target, maxDistance, closeCombat)) {
        player.dialogues.clear()
        player.movement.set(strategy) {
            if (player.cantReach(strategy) || player.movement.result == null) {
                player.message("You can't reach that.")
            } else if (player.movement.result is PathResult.Success) {
                if (player.action.suspension == Suspension.Movement) {
                    player.action.resume()
                }
            }
        }
        return false
    }
    return true
}