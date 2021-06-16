import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Died
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.move.cantReach
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy.Companion.isWithinAttackDistance
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.combat.getMaximumHit
import world.gregs.voidps.world.interact.entity.combat.hitChance

on<NPCClick>({ option == "Attack" }) { player: Player ->
    cancel = true
    attack(player, npc)
}

val npcs: NPCs by inject()

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
                val nextDelay = player.attackSwing(target)
                if (nextDelay < 0) {
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

fun canAttack(player: Player, target: Character): Boolean {
    if (npcs.getAtIndex(target.index) == null) {
        return false
    }
    if (target.action.type == ActionType.Death) {
        return false
    }
    // PVP area, slayer requirements, in combat etc..
    return true
}

fun withinRange(player: Player, target: Character): Boolean {
    val style = player.getVar("attack_style", 0)
    val type = player["combat_style", "melee"]
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

on<Command>({ prefix == "maxhit" }) { player: Player ->
    val weapon = player.equipped(EquipSlot.Weapon)
    player.message("Max hit")
    player.message("Ranged: ${getMaximumHit(player, null, "range", weapon)} Melee: ${getMaximumHit(player, null, "melee", weapon)} Magic: ${getMaximumHit(player, null, "spell", null)}")
    player.message("Hit chance")
    player.message("Ranged: ${hitChance(player, null, "range", weapon)} Melee: ${hitChance(player, null, "melee", weapon)} Magic: ${hitChance(player, null, "spell", null)}")
}