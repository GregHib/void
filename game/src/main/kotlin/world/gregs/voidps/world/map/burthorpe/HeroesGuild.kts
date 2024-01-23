package world.gregs.voidps.world.map.burthorpe

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnObject
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.world.interact.dialogue.type.item

on<ItemOnObject>({ operate && target.id == "fountain_of_heroes" && item.id == "amulet_of_glory" }) { player: Player ->
    if (player.inventory.replace(itemSlot, item.id, "amulet_of_glory_4")) {
        player.message("You dip the amulet in the fountain...")
        player.setAnimation("bend_down")
        item("amulet_of_glory", 300, "You feel a power emanating from the fountain as it recharges your amulet. You can now rub the amulet to teleport and wear it to get more gems whilst mining.")
    }
}