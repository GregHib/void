package content.skill.dungeoneering

import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class ItemBinding : Script {
    init {
        itemOption("Bind", "*") { (item, slot) ->
            if (!item.def.contains(Params.DUNGEONEERING) || item.def.contains(Params.DUNGEONEERING_BOUND_ITEM)) {
                message("This item cannot be bound.")
                return@itemOption
            }
            // TODO what happens if bind ammo with ammo already bound?
            // TODO what happens when unbinding
            // TODO what happens when binding?
            val dung = levels.getMax(Skill.Dungeoneering)
            val limit = when {
                dung >= 120 -> 4
                dung >= 100 -> 3
                dung >= 50 -> 2
                else -> 1
            }
            val bound = inventories.inventory("dungeoneering_bound")
            if (bound.count + 1 > limit) {
                message("Too many items bound") // TODO proper message
                return@itemOption
            }
            if (ItemDefinitions.get("${item.id}_bound").contains(Params.DUNGEONEERING_BOUND_AMMO)) {
                // TODO handle ammo
            } else {
                inventory.transaction {
                    if (item.amount == 1) {
                        remove(slot, item.id)
                        link(bound).add("${item.id}_bound")
                    } else {
                        // TODO store count in varbit for some reason?
//                    val removed = removeToLimit(item.id, 125)
//                    link(bound).add("${item.id}_bound", removed)
                    }
                }
            }
        }

        destroyed("*") { item ->
            if (item.def.contains(Params.DUNGEONEERING_BOUND_ITEM)) {
                val bound = inventories.inventory("dungeoneering_bound")
                bound.remove(item.id)
            } else if (item.def.contains(Params.DUNGEONEERING_BOUND_AMMO)) {
                // TODO
            }
        }
    }
}