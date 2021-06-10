import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Died
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.move.cantReach
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.*
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy.Companion.isWithinAttackDistance
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.func.toUnderscoreCase
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound
import kotlin.random.Random

on<NPCClick>({ option == "Attack" }) { player: Player ->
    cancel = true
    if (player.action.type == ActionType.Combat && player.hasEffect("skilling_delay")) {
        return@on
    }
    player.action(ActionType.Combat) {
        val moveHandler = npc.events.on<NPC, Moved> {
            val style = player.getVar("attack_style", 0)
            canAttack(player, npc, style)
        }
        val deathHandler = npc.events.on<NPC, Died> {
            cancel(ActionType.Combat)
        }
        try {
            player.watch(npc)
            while (isActive && player.awaitDialogues()) {
                if (player.hasEffect("skilling_delay")) {
                    delay(player.remaining("skilling_delay").toInt())
                }
                val style = player.getVar("attack_style", 0)
                if (!canAttack(player, npc, style)) {
                    await<Unit>(Suspension.Movement)
                }
                val nextDelay = if (style == 1) 3 else 4
                val weapon = player.equipped(EquipSlot.Weapon)
                val crossbow = weapon.name.endsWith("crossbow")
                player.setAnimation(if (crossbow) "crossbow_shoot" else "bow_shoot")
                if (!crossbow) {
                    player.setGraphic("bronze_arrow_fire", height = 100)
                }
                player.shoot(
                    name = if (crossbow) "crossbow_bolt" else "bronze_arrow",
                    target = npc,
                    delay = 40,
                    height = 43,
                    endHeight = npc.def["height", ShootProjectile.DEFAULT_HEIGHT],
                    curve = 8
                )
                val maxHit = 100
                val minHit = 0
                val damage = Random.nextInt(maxHit + 1).coerceAtLeast(minHit)
                delay(npc, 2) {
                    npc.hit(player, damage, Hit.Mark.Range)
                    npc.levels.drain(Skill.Constitution, damage)
                    npc["killer"] = player
                    val name = npc.def["category", npc.def.name.toUnderscoreCase()]
                    player.playSound("${name}_hit", delay = 40)
                    npc.setAnimation("${name}_hit")
                }
                player.start("skilling_delay", nextDelay, quiet = true)
            }
        } finally {
            npc.events.remove(moveHandler)
            npc.events.remove(deathHandler)
            player.watch(null)
            player.stop("skilling_delay")
        }
    }
}

fun canAttack(player: Player, npc: NPC, style: Int): Boolean {
    val maxDistance = if (style == 2) 9 else 7
    val closeCombat = false// ranged, magic or halberd
    val strategy = CombatTargetStrategy(npc, maxDistance, closeCombat)
    if (!isWithinAttackDistance(player.tile.x, player.tile.y, player.tile.plane, npc, maxDistance, closeCombat)) {
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