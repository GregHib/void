package content.area.kandarin.ardougne

import content.entity.combat.hit.damage
import content.entity.obj.door.enterDoor
import content.entity.sound.sound
import content.skill.thieving.Stole
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.add
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.queue.softQueue

@Script
class ArdougneChests {

    val tables: DropTables by inject()

    init {
        objectOperate("Open", "ardougne_locked_door_closed") {
            if (player.tile.x <= target.tile.x) {
                player.message("The door is locked.")
                player.sound("locked")
            } else {
                player.message("You go through the door.", ChatType.Filter)
                enterDoor(target)
            }
        }

        objectOperate("Pick-lock", "ardougne_locked_door_closed") {
            if (player.tile.x <= target.tile.x) {
                player.message("You attempt to pick the lock.", ChatType.Filter)
                if (Level.success(player.levels.get(Skill.Thieving), 50, 20)) {
                    player.message("You manage to pick the lock.", ChatType.Filter)
                    player.exp(Skill.Thieving, 3.5)
                } else {
                    player.message("You fail to pick the lock.")
                    return@objectOperate
                }
            } else {
                player.message("The door is already unlocked.")
                player.message("You go through the door.", ChatType.Filter)
            }
            enterDoor(target)
        }

        objectOperate("Open", "*_chest") {
            if (target.id == "steel_arrows_chest") {
                player.sound("locked")
                player.message("This chest is locked.")
                return@objectOperate
            }
            player.message("You have activated a trap on the chest.", ChatType.Filter)
            player.anim("human_lockedchest")
            player.sound("lever")
            when (target.id) {
                "coin_chest" -> player.damage(20 + player.levels.get(Skill.Constitution) / 10)
                "big_coin_chest" -> player.damage(40 + (player.levels.get(Skill.Constitution) * 0.15).toInt())
                "nature_chest" -> player.damage(30 + (player.levels.get(Skill.Constitution) * 0.12).toInt())
                "steel_arrows_chest" -> player.damage((player.levels.get(Skill.Constitution) + 225) / 50)
                "blood_rune_chest" -> player.damage((player.levels.get(Skill.Constitution) + 225) / 50)
            }
        }

        objectOperate("Search for traps", "*_chest") {
            if (player.hasClock(target.id)) {
                return@objectOperate
            }
            val level: Int = def.getOrNull("level") ?: return@objectOperate
            if (!player.has(Skill.Thieving, level)) {
                return@objectOperate
            }
            if (target.id == "steel_arrows_chest") {
                player.message("You attempt to pick the lock.", type = ChatType.Filter)
                if (!player.inventory.contains("lockpick")) {
                    player.message("You need a lockpick for this lock.", type = ChatType.Filter)
                    return@objectOperate
                }
                delay(1)
                player.message("You manage to pick the lock.", type = ChatType.Filter)
            } else {
                player.message("You search the chest for traps.", type = ChatType.Filter)
                player.message("You find a trap on the chest, ", type = ChatType.Filter)
                delay(1)
                player.message("You disable the trap.", type = ChatType.Filter)
            }
            player.sound("locked")
            delay(1)
            player.message("You open the chest.", type = ChatType.Filter)
            player.anim("human_lockedchest")
            player.sound("chest_open")
            delay(1)
            target.replace("trap_chest_open", ticks = 3)
            delay(1)
            val table = tables.get("${target.id}_drop_table")
            if (table != null) {
                val drops = table.role().map { it.toItem() }
                player.inventory.transaction {
                    for (item in drops) {
                        add(item.id, item.amount)
                    }
                }
                when (player.inventory.transaction.error) {
                    is TransactionError.Full -> player.inventoryFull()
                    TransactionError.None -> for (item in drops) {
                        Stole.stole(player, target, item)
                    }
                    else -> return@objectOperate
                }
            }
            player.anim("climb_down")
            player.anim("open_chest")
            delay(1)
            player.message("You find treasure inside!", ChatType.Filter)
            val exp: Double = def.getOrNull("exp") ?: return@objectOperate
            player.exp(Skill.Thieving, exp)
            val restock: Int = def.getOrNull("restock") ?: return@objectOperate
            target.replace("trap_chest_empty", ticks = restock)
        }

        objectOperate("Open", "trap_chest_empty") {
            player.message("It looks like this chest has already been looted.", type = ChatType.Game)
        }

        objectOperate("Search for traps", "trap_chest_empty") {
            player.message("It looks like this chest has already been looted.", type = ChatType.Game)
        }
    }
}