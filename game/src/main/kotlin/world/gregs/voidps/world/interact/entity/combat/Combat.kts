import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Died
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.move.cantReach
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy.Companion.isWithinAttackDistance
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.attackStyle
import world.gregs.voidps.world.interact.entity.combat.canAttack

on<NPCClick>({ option == "Attack" }) { player: Player ->
    cancel = true
    attack(player, npc)
}

fun attack(player: Player, target: Character) {
    player.action(ActionType.Combat) {
        player["target"] = target
        val moveHandler = target.events.on<NPC, Moved> {
            withinRange(player, target)
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
                if (!withinRange(player, target)) {
                    await<Unit>(Suspension.Movement)
                }
                if (!canAttack(player, target)) {
                    break
                }
                val swing = CombatSwing(target)
                player.events.emit(swing)
                val nextDelay = swing.delay
                if (nextDelay == null || nextDelay < 0) {
                    break
                }
                player.start("skilling_delay", nextDelay, quiet = true)
            }
        } finally {
            player.clear("target")
            target.events.remove(moveHandler)
            target.events.remove(deathHandler)
            player.watch(null)
        }
    }
}

fun withinRange(player: Player, target: Character): Boolean {
    val maxDistance = (player["attack_range", 1] + if (player.attackStyle == "long_range") 2 else 0).coerceAtMost(10)
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