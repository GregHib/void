package content.skill.thieving

import content.entity.combat.hit.damage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add

class TrapChests(val tables: DropTables) : Script {

    init {
        objectOperate("Open", "*_chest") { (target) ->
            when (target.id) {
                "coin_chest" -> damage(20 + levels.get(Skill.Constitution) / 10)
                "big_coin_chest" -> damage(40 + (levels.get(Skill.Constitution) * 0.15).toInt())
                "nature_chest" -> damage(30 + (levels.get(Skill.Constitution) * 0.12).toInt())
                "steel_arrows_chest" -> damage((levels.get(Skill.Constitution) + 225) / 50)
                "blood_rune_chest" -> damage((levels.get(Skill.Constitution) + 225) / 50)
                else -> {
                    sound("locked")
                    message("This chest is locked.")
                    return@objectOperate
                }
            }
            message("You have activated a trap on the chest.", ChatType.Filter)
            anim("human_lockedchest")
            sound("lever")
        }

        objectOperate("Search for traps", "*_chest") { (target) ->
            if (hasClock(target.id)) {
                return@objectOperate
            }
            val level: Int = target.def.getOrNull("level") ?: return@objectOperate
            if (!has(Skill.Thieving, level)) {
                return@objectOperate
            }
            if (target.id == "steel_arrows_chest" || target.id.startsWith("dorgesh_kaan")) {
                message("You attempt to pick the lock.", type = ChatType.Filter)
                if (!inventory.contains("lockpick")) {
                    message("You need a lockpick for this lock.", type = ChatType.Filter)
                    return@objectOperate
                }
                delay(1)
                message("You manage to pick the lock.", type = ChatType.Filter)
            } else {
                message("You search the chest for traps.", type = ChatType.Filter)
                message("You find a trap on the chest, ", type = ChatType.Filter)
                delay(1)
                message("You disable the trap.", type = ChatType.Filter)
            }
            sound("locked")
            delay(1)
            message("You open the chest.", type = ChatType.Filter)
            anim("human_lockedchest")
            sound("chest_open")
            delay(1)
            var name = if (target.id.startsWith("dorgesh_kaan")) target.id else "trap_chest"
            target.replace("${name}_open", ticks = 3)
            delay(1)
            val table = tables.get("${target.id}_drop_table")
            if (table != null) {
                val drops = table.roll().map { it.toItem() }
                inventory.transaction {
                    for (item in drops) {
                        add(item.id, item.amount)
                    }
                }
                when (inventory.transaction.error) {
                    is TransactionError.Full -> inventoryFull()
                    TransactionError.None -> for (item in drops) {
                        Stole.stole(this, target, item)
                    }
                    else -> return@objectOperate
                }
            }
            anim("climb_down")
            anim("open_chest")
            delay(1)
            message("You find treasure inside!", ChatType.Filter)
            val exp: Double = target.def.getOrNull("exp") ?: return@objectOperate
            exp(Skill.Thieving, exp)
            val restock: Int = target.def.getOrNull("restock") ?: return@objectOperate
            target.replace("${name}_empty", ticks = restock)
        }

        objectOperate("Open", "trap_chest_empty") {
            message("It looks like this chest has already been looted.", type = ChatType.Game)
        }

        objectOperate("Search for traps", "trap_chest_empty") {
            message("It looks like this chest has already been looted.", type = ChatType.Game)
        }
    }
}
