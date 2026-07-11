package content.skill.fishing

import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

/**
 * Caskets from big net fishing and mystery boxes hold a little treasure: usually coins or an
 * uncut gem, with a slim shot at a key half.
 * https://runescape.wiki/w/Casket
 */
class Casket : Script {

    init {
        itemOption("Open", "casket") { (item, slot) ->
            if (!inventory.remove(slot, item.id)) {
                return@itemOption
            }
            sound("casket_open")
            val drop = get<DropTables>().getValue("casket").roll(player = this).firstOrNull()?.toItem()
            if (drop == null || drop.isEmpty()) {
                return@itemOption
            }
            addOrDrop(drop.id, drop.amount)
            val name = drop.def.name.lowercase()
            val found = when {
                drop.amount > 1 -> "${drop.amount} x $name"
                name.first() in "aeiou" -> "an $name"
                else -> "a $name"
            }
            message("You open the casket and find $found.")
        }
    }
}
