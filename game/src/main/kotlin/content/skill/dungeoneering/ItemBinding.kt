package content.skill.dungeoneering

import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.removeToLimit
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class ItemBinding : Script {
    init {
        itemOption("Bind", "*") { (item, slot) ->
            if (!item.def.contains(Params.DUNGEONEERING) || item.def.contains(Params.DUNGEONEERING_BOUND_ITEM)) {
                message("This item cannot be bound.")
                return@itemOption
            }
            if (ItemDefinitions.get("${item.id}_bound").contains(Params.DUNGEONEERING_BOUND_AMMO)) {
                when (get("dungeoneering_bound_ammo_id", "")) {
                    "" -> {
                        var count = 0
                        inventory.transaction {
                            count = removeToLimit(item.id, 125)
                            add("${item.id}_bound", count)
                        }
                        when (inventory.transaction.error) {
                            TransactionError.None -> {
                                message("You now have $count ammo bound.")
                                set("dungeoneering_bound_ammo_id", "${item.id}_bound")
                                set("dungeoneering_bound_ammo_count", count)
                            }
                            else -> ammoFull()
                        }
                    }
                    "${item.id}_bound" -> {
                        val amount = get("dungeoneering_bound_ammo_count", 0)
                        val space = 125 - amount
                        if (space <= 0) {
                            ammoFull()
                            return@itemOption
                        }
                        var count = 0
                        inventory.transaction {
                            count = removeToLimit(item.id, space)
                            add("${item.id}_bound", count)
                        }
                        when (inventory.transaction.error) {
                            TransactionError.None -> {
                                val total = inc("dungeoneering_bound_ammo_count", count)
                                message("You now have $total ammo bound.")
                            }
                            else -> ammoFull()
                        }
                    }
                    else -> ammoFull()
                }
            } else {
                val dung = levels.getMax(Skill.Dungeoneering)
                val limit = when {
                    dung >= 120 -> 4
                    dung >= 100 -> 3
                    dung >= 50 -> 2
                    else -> 1
                }
                val bound = inventories.inventory("dungeoneering_bound")
                if (bound.count + 1 > limit) {
                    message("Your bound inventory is full.")
                    return@itemOption
                }
                // https://youtu.be/sduDxzoFoZU?t=46
                inventory.transaction {
                    replace(slot, item.id, "${item.id}_bound")
                    link(bound).add("${item.id}_bound")
                }
                when (inventory.transaction.error) {
                    TransactionError.None -> message("You bind the ${item.def.name} to you.")
                    else -> {}
                }
            }
        }

        destroyed("*") { item ->
            if (item.def.contains(Params.DUNGEONEERING_BOUND_ITEM)) {
                val bound = inventories.inventory("dungeoneering_bound")
                bound.remove(item.id)
            } else if (item.def.contains(Params.DUNGEONEERING_BOUND_AMMO)) {
                clear("dungeoneering_bound_ammo_id")
                clear("dungeoneering_bound_ammo_count")
            }
        }
    }

    private fun Player.ammoFull() {
        // https://youtu.be/AZtXwFWWiP8?t=82
        message("<red>Your bound ammo slots are full.")
    }
}
