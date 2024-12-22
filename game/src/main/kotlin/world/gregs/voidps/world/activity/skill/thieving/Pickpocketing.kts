package world.gregs.voidps.world.activity.skill.thieving

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.data.Pocket
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.success
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.drop.DropTable
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.world.activity.skill.slayer.race
import world.gregs.voidps.world.interact.entity.effect.stun
import world.gregs.voidps.world.interact.entity.npc.combat.NPCAttack

val animationDefinitions: AnimationDefinitions by inject()
val dropTables: DropTables by inject()
val logger = InlineLogger()

npcOperate("Pickpocket") {
    if (player.inventory.isFull()) {
        player.inventoryFull()
        return@npcOperate
    }
    if (player.hasClock("food_delay") || player.hasClock("action_delay")) { // Should action_delay and food_delay be the same??
        return@npcOperate
    }
    val pocket: Pocket = target.def.getOrNull("pickpocket") ?: return@npcOperate
    if (!player.has(Skill.Thieving, pocket.level)) {
        return@npcOperate
    }
    player.start("delay", 3)
    player.start("movement_delay", 3)
    val name = target.def.name
    player.message("You attempt to pick the ${name}'s pocket.", ChatType.Filter)
    target.start("movement_delay", 1)
    player.softQueue("pick-pocket", 2) {
        if (success(player.levels.get(Skill.Thieving), pocket.chance)) {
            player.setAnimation("pick_pocket")
            player.message("You pick the ${name}'s pocket.", ChatType.Filter)
            player.exp(Skill.Thieving, pocket.xp)
            giveLoot(player, target)
        } else {
            target.face(player)
            target.forceChat = pocket.caughtMessage
            target.setAnimation(NPCAttack.animation(target, animationDefinitions))
            player.message("You fail to pick the ${name}'s pocket.", ChatType.Filter)
            target.stun(player, pocket.stunTicks, pocket.stunHit)
        }
    }
}

fun giveLoot(player: Player, target: NPC) {
    var table: DropTable? = dropTables.get("${target.id}_pickpocket")
    if (table == null) {
        table = dropTables.get("${target.race}_pickpocket")
    }
    if (table == null) {
        return
    }
    val loot = table.role(members = World.members)
    player.inventory.transaction {
        for (drop in loot) {
            val item = drop.toItem()
            add(item.id, item.amount)
        }
    }
    when (player.inventory.transaction.error) {
        is TransactionError.Full -> player.inventoryFull()
        TransactionError.None -> {}
        else -> logger.warn { "Unable to add pickpocket loot $player $loot" }
    }
}