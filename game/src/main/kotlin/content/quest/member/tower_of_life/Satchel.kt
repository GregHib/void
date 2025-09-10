package content.quest.member.tower_of_life

import content.entity.player.dialogue.type.item
import content.entity.player.inv.inventoryItem
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.SetCharge.setCharge
import world.gregs.voidps.engine.event.Script
@Script
class Satchel {

    val cake = 0x1
    val banana = 0x2
    val sandwich = 0x4
    
    init {
        inventoryItem("Inspect", "*_satchel") {
            val charges = player.inventory.charges(player, slot)
            val cake = if (charges and cake != 0) "one" else "no"
            val banana = if (charges and banana != 0) "one" else "no"
            val sandwich = if (charges and sandwich != 0) "one" else "no"
            item(item.id, 400, "The ${item.id.toLowerSpaceCase()}!<br>(Containing: $sandwich sandwich, $cake cake, and $banana banana)")
        }

        inventoryItem("Empty", "*_satchel") {
            var charges = player.inventory.charges(player, slot)
            charges = withdraw(player, slot, charges, "banana", banana)
            charges = withdraw(player, slot, charges, "cake", cake)
            withdraw(player, slot, charges, "triangle_sandwich", sandwich)
        }

        itemOnItem("cake", "*_satchel") { player ->
            val charges = player.inventory.charges(player, toSlot)
            if (charges and cake != 0) {
                player.message("You already have a cake in there.")
                return@itemOnItem
            }
            player.inventory.transaction {
                remove(fromSlot, "cake")
                setCharge(toSlot, charges + cake)
            }
        }

        itemOnItem("banana", "*_satchel") { player ->
            val charges = player.inventory.charges(player, toSlot)
            if (charges and banana != 0) {
                player.message("You already have a banana in there.")
                return@itemOnItem
            }
            player.inventory.transaction {
                remove(fromSlot, "banana")
                setCharge(toSlot, charges + banana)
            }
        }

        itemOnItem("triangle_sandwich", "*_satchel") { player ->
            val charges = player.inventory.charges(player, toSlot)
            if (charges and sandwich != 0) {
                player.message("You already have a sandwich in there.")
                return@itemOnItem
            }
            player.inventory.transaction {
                remove(fromSlot, "triangle_sandwich")
                setCharge(toSlot, charges + sandwich)
            }
        }

    }

    fun withdraw(player: Player, slot: Int, charges: Int, id: String, food: Int): Int {
        if (charges and food != 0) {
            val success = player.inventory.transaction {
                add(id)
                setCharge(slot, charges and food.inv())
            }
            if (success) {
                return charges and food.inv()
            } else {
                player.message("You don't have enough free space to empty your satchel.")
            }
        }
        return charges
    }
    
}
