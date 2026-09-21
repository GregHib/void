package content.area.misthalin.lumbridge.blood_pact

import content.entity.player.dialogue.type.item
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class DragithMask : Script {

    init {
        val masks = "mask_part_1,mask_part_2,mask_part_3,mask_part_4,mask_part_5"
        itemOnItem(masks, masks, bidirectional = false) { fromItem, toItem ->
            if (fromItem.id == toItem.id) {
                return@itemOnItem
            }
            if (inventory.count("mask_part_1") < 1 || inventory.count("mask_part_2") < 1 || inventory.count("mask_part_3") < 1 || inventory.count("mask_part_4") < 1 || inventory.count("mask_part_5") < 1) {
                message("You need all five parts of the mask in order to combine them.")
                return@itemOnItem
            }
            val success = inventory.transaction {
                remove("mask_part_1")
                remove("mask_part_2")
                remove("mask_part_3")
                remove("mask_part_4")
                remove("mask_part_5")
                add("mask_of_dragith_nurn")
            }
            if (!success) {
                return@itemOnItem
            }
            item(item = "mask_of_dragith_nurn", text = "You combine the parts and assemble the Mask of Dragith Nurn.")
        }
    }
}
