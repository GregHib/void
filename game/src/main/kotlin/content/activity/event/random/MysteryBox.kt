package content.activity.event.random

import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

/**
 * Mystery box: the surprise choice from a random event gift. Opening it rolls the `mystery_box`
 * drop table - usually an oddment, with a slim shot at the rare drop table.
 * https://runescape.wiki/w/Mystery_box_(random_events)
 */
class MysteryBox : Script {

    init {
        itemOption("Open", "mystery_box") { (item, slot) ->
            if (!inventory.remove(slot, item.id)) {
                return@itemOption
            }
            val drop = get<DropTables>().getValue("mystery_box").roll(player = this).firstOrNull()?.toItem()
            if (drop == null || drop.isEmpty()) {
                message("Inside the box you find nothing! Better luck next time!")
                return@itemOption
            }
            addOrDrop(drop.id, drop.amount)
            val name = drop.def.name.lowercase()
            val found = when {
                drop.amount > 1 -> "${drop.amount} x $name"
                name.first() in "aeiou" -> "an $name"
                else -> "a $name"
            }
            val flavour = when (drop.def.cost * drop.amount) {
                in 0..100 -> "Better luck next time!"
                in 101..500 -> "Well, it could have been worse."
                else -> "Excellent!"
            }
            message("Inside the box you find $found! $flavour")
        }
    }
}
