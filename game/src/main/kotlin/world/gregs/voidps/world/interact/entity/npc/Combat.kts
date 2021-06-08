import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.move.cantReach
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy.Companion.isWithinAttackDistance
import world.gregs.voidps.network.encode.message

on<NPCClick>({ option == "Attack" }) { player: Player ->
    cancel = true
    if (player.action.type == ActionType.Combat && player.hasEffect("skilling_delay")) {
        return@on
    }
    player.action(ActionType.Combat) {
        val handler = npc.events.on<NPC, Moved> {
            canAttack(player, npc)
        }
        try {
            player.watch(npc)
            while (isActive && player.awaitDialogues()) {
                if (player.hasEffect("skilling_delay")) {
                    delay(player.remaining("skilling_delay").toInt())
                }
                if (!canAttack(player, npc)) {
                    await<Unit>(Suspension.Movement)
                }
                val nextDelay = 4
                player.setAnimation("bow_shoot")
                player.start("skilling_delay", nextDelay, quiet = true)
            }
        } finally {
            npc.events.remove(handler)
            player.stop("skilling_delay")
        }
    }
}

fun canAttack(player: Player, npc: NPC): Boolean {
    val maxDistance = 7
    val closeCombat = false// ranged, magic or halberd
    val strategy = CombatTargetStrategy(npc, maxDistance, closeCombat)
    if (!isWithinAttackDistance(player.tile.x, player.tile.y, player.tile.plane, npc, maxDistance, closeCombat)) {
        player.dialogues.clear()
        player.movement.clear()
        player.movement.result = null
        player.movement.strategy = strategy
        player["in_combat"] = false
        player.movement.action = {
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