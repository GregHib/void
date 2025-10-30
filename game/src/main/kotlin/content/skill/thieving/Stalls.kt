package content.skill.thieving

import content.entity.combat.inCombat
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class Stalls : Script {

    val drops: DropTables by inject()

    init {
        objectOperate("Steal-from", "*_stall") {
            if (player.inCombat) {
                player.message("You can't do this while you're in combat.")
                return@objectOperate
            }
            if (player.equipped(EquipSlot.Weapon).id.endsWith("_greegree")) {
                player.message("You wouldn’t like to blow your cover by getting caught stealing.")
                return@objectOperate
            }
            val level: Int = def.getOrNull("level") ?: return@objectOperate
            if (!player.has(Skill.Thieving, level, " to steal from this stall")) {
                return@objectOperate
            }
            if (player.inventory.isFull()) {
                player.inventoryFull()
                return@objectOperate
            }
            player.start("thieving", 2)
            player["stall_level"] = level
            player.anim("take")
            delay(2)
            val chance: String? = def.getOrNull("chance")
            if (chance != null) {
                val range = chance.toIntRange(inclusive = true)
                if (!Level.success(player.levels.get(Skill.Thieving), range)) {
                    player.message("You attempt to steal from the stall but you miss your chance...")
                    return@objectOperate
                }
            }
            if (!player.hasClock("${target.id}_thief")) {
                player.start("${target.id}_thief", TimeUnit.MINUTES.toTicks(random.nextInt(20, 30)))
            }
            val table = drops.get("${target.id}_drop_table")
            if (table != null) {
                val drops = table.role().map { it.toItem() }
                player.inventory.transaction {
                    for (item in drops) {
                        add(item.id, item.amount)
                    }
                }
                when (player.inventory.transaction.error) {
                    is TransactionError.Full -> {
                        player.inventoryFull()
                        return@objectOperate
                    }
                    TransactionError.None -> for (item in drops) {
                        Stole.stole(player, target, item)
                    }
                    else -> {}
                }
            }
            val exp: Double = def.getOrNull("exp") ?: return@objectOperate
            player.exp(Skill.Thieving, exp)
            val restock: Int = def.getOrNull("restock") ?: return@objectOperate
            target.replace("${target.id}_empty", ticks = restock)
        }
    }
}
