package content.area.asgarnia.burthorpe

import content.entity.player.dialogue.type.item
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

@Script
class HeroesGuild {

    init {
        itemOnObjectOperate("amulet_of_glory", "fountain_of_heroes") {
            if (player.inventory.replace(itemSlot, item.id, "amulet_of_glory_4")) {
                player.message("You dip the amulet in the fountain...")
                player.anim("human_pickupfloor")
                item("amulet_of_glory", 300, "You feel a power emanating from the fountain as it recharges your amulet. You can now rub the amulet to teleport and wear it to get more gems whilst mining.")
            }
        }
    }
}
